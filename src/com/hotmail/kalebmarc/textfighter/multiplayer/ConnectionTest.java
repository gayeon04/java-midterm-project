package com.hotmail.kalebmarc.textfighter.multiplayer;

/**
 * 소켓 연결 확인용 테스트.
 *
 * 터미널 1:  java ConnectionTest server
 * 터미널 2:  java ConnectionTest client
 */
public class ConnectionTest {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("사용법: ConnectionTest server  OR  ConnectionTest client");
            return;
        }
        if (args[0].equals("server")) runServer();
        else                          runClient();
    }

    private static void runServer() throws Exception {
        System.out.println("[서버] 포트 9999 대기 중...");
        BattleServer server = new BattleServer();
        server.start();
        System.out.println("[서버] 클라이언트 연결됨");

        String msg = server.receiveMessage();
        System.out.println("[서버] 수신: " + msg);

        server.sendMessage(BattleMessage.ready());
        System.out.println("[서버] READY 전송 완료");
        server.close();
    }

    private static void runClient() throws Exception {
        System.out.println("[클라이언트] 서버 연결 중...");
        BattleClient client = new BattleClient();
        client.connect("localhost");
        System.out.println("[클라이언트] 연결 성공");

        client.sendMessage(BattleMessage.ready());
        System.out.println("[클라이언트] READY 전송");

        String response = client.receiveMessage();
        System.out.println("[클라이언트] 서버 응답: " + response);
        client.close();
    }
}
