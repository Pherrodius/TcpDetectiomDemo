package com.example.demo.controller;
import com.example.demo.pojo.ISocket;
import com.example.demo.pojo.MultiDetectionDTO;
import com.example.demo.pojo.Result;
import com.example.demo.pojo.SingleDetcetionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@Tag(name = "端口检测 API")
@RequiredArgsConstructor
public class TcpController {
    private final ThreadPoolTaskExecutor executor;


    @GetMapping("/tcp/single")
    @Operation(summary = "检测某IP/域名在指定端口区间是否开放", description = "输入要检测的IP/域名和端口号范围，返回开放的端口号列表")
       public Result<SingleDetcetionVO> detect(@RequestParam(name = "path", defaultValue = "localhost") String path,
                                               @RequestParam(name = "startPort", defaultValue = "0") int startPort,
                                               @RequestParam(name = "endPort", defaultValue = "1024") int endPort,
                                               @RequestParam(name = "timeout", defaultValue = "1000") int timeout) {
        if(startPort < 0 || endPort < 0 || timeout < 0) {
            return Result.error("端口号范围和超时时间不能小于0");
        }
        if(startPort > endPort) {
            return Result.error("开始端口号不能大于结束端口号");
        }
        if(endPort>65536) {
            return Result.error("端口号不能大于65536");
        }
        var passablePortList = new ArrayList<Integer>();
        AtomicInteger PortCount = new AtomicInteger(0);

        if(path.startsWith("http://")||path.startsWith("https://") ) {
            path =path.substring(path.indexOf("://")+3);
        }
        if(path.contains("/")) {
            path = path.substring(0, path.indexOf("/"));
        }
        String finalPath = path;

        for (int port = startPort; port <= endPort; port++) {
            int finalPort = port;
            try {
                executor.execute(() -> {
                    try (Socket socket = new Socket()) {
                        socket.connect(new InetSocketAddress(finalPath, finalPort), timeout);
                        synchronized (passablePortList) {
                            System.out.println(finalPath+"端口 " + finalPort + " 开放");
                            passablePortList.add(finalPort);
                            PortCount.incrementAndGet();
                        }
                    } catch (IOException e) {
                        PortCount.incrementAndGet();
                        System.out.println(finalPath+"端口 " + finalPort + " 关闭");
                    }
                });
            } catch (Exception e) {
                return Result.error("线程池繁忙，请稍后再试");
            }
        }
        while (PortCount.get() < endPort - startPort + 1) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return Result.success(SingleDetcetionVO.builder()
                .path(path)
                .portRange(startPort + "-" + endPort)
                .passablePortList(passablePortList)
                .passablePortCount(passablePortList.size())
                .build());
    }


    @PostMapping("/tcp/multi")
    @Operation(summary = "批量检测IP/域名端口组合是否开放", description = "接收要检测的端口组合列表，返回开放的地址和端口组合")
    public Result<ArrayList<ISocket>> detectBatch(@RequestBody MultiDetectionDTO request) {
        List<ISocket> socketList = request.getSocketList();
        int timeout = request.getTimeout();
        var passablePortList = new ArrayList<ISocket>();
        AtomicInteger count = new AtomicInteger(0);
        for (var Isocket : socketList) {
            try {
                executor.execute(() -> {
                    int port = Isocket.getPort();
                    String path = Isocket.getPath();
                    if(path.startsWith("http://")||path.startsWith("https://") ) {
                        path =path.substring(path.indexOf("://")+3);
                    }
                    if(path.contains("/")) {
                        path = path.substring(0, path.indexOf("/"));
                    }
                    String finalPath = path;
                    try (Socket socket = new Socket()) {
                        socket.connect(new InetSocketAddress(finalPath, port), timeout);
                        System.out.println(finalPath+"端口 " + port + " 开放");
                        synchronized (passablePortList) {
                            Isocket.setPassable(true);
                            passablePortList.add(Isocket);
                        }
                    } catch (IOException e) {
                        System.out.println(path+"端口 " + port + " 关闭");
                        synchronized (passablePortList) {
                            Isocket.setPassable(false);
                            passablePortList.add(Isocket);
                        }
                    } finally {
                        count.incrementAndGet();
                    }
                });
            } catch (Exception e) {
                return Result.error("线程池繁忙，请稍后再试");
            }
        }
        while (count.get() < socketList.size()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return Result.success(passablePortList);
    }
}
