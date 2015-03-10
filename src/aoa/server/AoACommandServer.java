package aoa.server;

public class AoACommandServer 
{
    public static void main(String[] args) throws Exception
    {        
      new AoAHttpServer(new AoAMessageServer(args)).start();
    }
}
