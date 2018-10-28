public class FWebServer {

    public static void main(String[] args) {
        if (args.length != 0) {
            try {
                int port = Integer.parseInt(args[0]);

                new Server(port);
            }
            catch(NumberFormatException e) {
                e.printStackTrace();
                System.exit(0);
            }
        } else {
            System.exit(1);
        }
    }
}
