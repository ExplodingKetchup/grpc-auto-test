//package org.grpctest.core.service.ui;
//
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.grpctest.core.enums.MetadataType;
//import org.grpctest.core.pojo.RuntimeConfig;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@AllArgsConstructor
//@Slf4j
//public class TestSetupUiTerminalImpl implements TestSetupUi {
//    @Override
//    public void printHelloMessage() {
//        System.out.println("Welcome to gRPC Testing tool");
//        System.out.println("This tool tests gRPC remote procedural calls between server and client programs written in different languages.");
//        System.out.println("-----------------------------------------------");
//    }
//
//    @Override
//    public RuntimeConfig.Language chooseServer() throws Exception {
//        System.out.println("Please select a server:");
//        return null;
//    }
//
//    @Override
//    public RuntimeConfig.Language chooseClient() throws Exception {
//        return null;
//    }
//
//    @Override
//    public MetadataType chooseServerToClientMetadataType() throws Exception {
//        return null;
//    }
//
//    @Override
//    public MetadataType chooseClientToServerMetadataType() throws Exception {
//        return null;
//    }
//
//    @Override
//    public boolean chooseEnableException() throws Exception {
//        return false;
//    }
//
//    @Override
//    public boolean chooseEnableGeneratedTestcase() throws Exception {
//        return false;
//    }
//
//    @Override
//    public List<String> chooseIncludedRpcFiles() throws Exception {
//        return null;
//    }
//
//    @Override
//    public int chooseOmitFieldsInRandomTestcases() throws Exception {
//        return 0;
//    }
//}
