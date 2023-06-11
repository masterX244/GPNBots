package GPN;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class BotGame {
    private boolean switchItharder = false;
    private Queue<String> returnMsgs = new LinkedList<>();
    Object syncer = new Object();
    static Logger l = LogManager.getLogger();
    private int[] []board = new int[1][1];
    int[][] boards;
    int[][] boardFF;
    int ticksSinceStart = 0;
    int[][] tickets = null;
    boolean danger = false;
    private int cx=0,cy=0,cw=1,ch=1;
    private String playerid;
    int playercount = 0;
    boolean[][] headBuffer;
    int lastDir = 0;
    String debugdir = " ";
    boolean gaming = false;
    boolean switcheroo=false;
    public static void main(String[] args) {
        setupLogging(true,2);
        l.info("shizzlemork");
        new BotGame().game();
    }
    int lastcase=-1;

    private int boardFF(int x, int y, int id)
    {
        return boardFF(x,y,id,0);
    }


    Queue<int[]> FFQueue = new LinkedList<>();
    private int boardFF(int x, int y,int id,int g)
    {
        FFQueue.add(new int[]{x,y,g});
        return boardFF(id);
    }
    int heads_touched = 0;
    private int boardFF(int  id)
    {
        int rv_r=0;
        while(!FFQueue.isEmpty())
        {
            int rv=0;
            int[] ffq = FFQueue.remove();
            int x=ffq[0];
            int y=ffq[1];
            int g=ffq[2];
            if(boardFF[y][x]!=0)
            {
                if(boardFF[y][x]>0&&boardFF[y][x]!=id&&g==0)
                {
                    l.info("1337#d");
                    rv_r += -1337;
                    continue;
                }
                rv_r+=0;
                continue;
            }

            rv = 2 ;
            int cni = y-1;
            if(cni==-1)
                cni=ch-1;
            int csi=(y+1)%ch;

            int cwi = x-1;
            if(cwi==-1)
                cwi=cw-1;
            int cei=(x+1)%cw;
            if(headBuffer[y][cwi]||headBuffer[y][cei]||headBuffer[csi][x]||headBuffer[cni][x])
            {
                rv_r+= g==0?1:0;
                heads_touched++;
                continue;
            }
            boardFF[y][x]=id;
            if(g<15) {

                FFQueue.add(new int[]{x, cni, g + 1});
                FFQueue.add(new int[]{x, csi, g + 1});
                FFQueue.add(new int[]{cwi, y, g + 1});
                FFQueue.add(new int[]{cei, y, g + 1});
            }
            rv_r+=rv;
        }
        return rv_r;
    }

    static void setupLogging(boolean verbose,int verbosityLevel)
    {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager"); //HACK to catch java.util.logging loggers
        l=LogManager.getLogger();
        LoggerContext cx = (LoggerContext) LogManager.getContext(false);
        org.apache.logging.log4j.core.config.Configuration config = cx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        LoggerConfig vlcj = config.getLoggerConfig("uk.co.caprica.vlcj");
        LoggerConfig externalloggerConfig = config.getLoggerConfig("External");
        LoggerConfig trueVFSlloggerConfig = config.getLoggerConfig("net.java");

        if (verbose)
        {
            loggerConfig.setLevel(Level.TRACE);

            if(verbosityLevel>1)
            {
                vlcj.setLevel(Level.TRACE);
                externalloggerConfig.setLevel(Level.TRACE);
                trueVFSlloggerConfig.setLevel(Level.TRACE);
            }
            else
            {
                vlcj.setLevel(Level.INFO);
                externalloggerConfig.setLevel(Level.INFO);
                trueVFSlloggerConfig.setLevel(Level.INFO);
            }
        }
        else
        {
            loggerConfig.setLevel(Level.INFO);
            externalloggerConfig.setLevel(Level.INFO);
            trueVFSlloggerConfig.setLevel(Level.INFO);
        }
        cx.updateLoggers();
    }

    public void game(){
        returnMsgs.add("join|masterX244|notthepassword");
        try {
            Socket sck=new Socket("gpn-tron.duckdns.org", 4000);
            BufferedReader r = new BufferedReader(new InputStreamReader(sck.getInputStream()));
            PrintStream out = new PrintStream(sck.getOutputStream(),true,"UTF-8");
            Thread t = new Thread(()->{
                while(sck.isConnected())
                {
                    //l.trace("Munching ahead...");
                    String request = "";
                    try {
                        String ln = r.readLine();
                        //if(gaming)
                        //    l.trace("packet line = "+ln);
                        if(ln!=null)
                        {
                            if(ln.contains("message"))
                            {
                                continue;
                            }
                            if(ln.contains("lose"))
                            {
                                String boardview = "\nDumping Core....\nDone:\n";
                                for(int i=0;i<ch;i++)
                                {
                                    String secondaryline = "";
                                    for(int j=0;j<cw;j++)
                                    {
                                        boolean self =false;
                                        if(i==cy&&j==cx)
                                        {
                                            self=true;
                                            boardview+="XXXX";
                                        }
                                        String number ="000"+boards[i][j]+"|";
                                        String ticket = "000"+tickets[i][j]+"|";
                                        String hbfr = headBuffer[i][j]?"#":"_";
                                        if(self)
                                        {
                                            hbfr=debugdir;
                                        }
                                        boardview+=hbfr+
                                                number.substring(number.length()-4,number.length());
                                        secondaryline+=ticket.substring(ticket.length()-5,ticket.length());
                                    }
                                    boardview+="\n"+secondaryline+"\n";
                                }l.info("LOSER!!!1111elf");
                                l.info(boardview+"\nEnd of Dump");


                                gaming=false;
                            }
                            if(ln.contains("game"))
                            {

                                gaming=true;
                                ticksSinceStart =0;

                                lastcase=-1;
                                l.info(ln);
                                String[] ls = ln.split("\\|");
                                int w = Integer.valueOf(ls[1]);
                                int h = Integer.valueOf(ls[2]);
                                board = new int[w][h];
                                cw=w;ch=h;
                                playerid=ls[3];
                                boards=new int[h][w];
                                boardFF=new int[h][w];
                                headBuffer = new boolean[h][w];
                                returnMsgs.add("chat|Red 5 standing by");
                                tickets = new int[h][w];
                            }
                            if(ln.contains("pos")&&gaming)
                            {
                                if(playercount==0)
                                {
                                    headBuffer = new boolean[ch][cw];
                                }
                                playercount++;
                                String[] ls = ln.split("\\|");
                                int w = Integer.valueOf(ls[2]);
                                int h = Integer.valueOf(ls[3]);
                                if(ls[1].equals(playerid))
                                {
                                    l.info("Self");
                                    cx=w;cy=h;
                                    int cni = cy-1;
                                    if(cni==-1)
                                        cni=ch-1;
                                    int csi=(cy+1)%ch;

                                    int cwi = cx-1;
                                    if(cwi==-1)
                                        cwi=cw-1;
                                    int cei=(cx+1)%cw;
                                    boardFF[cni][cwi]=-1;
                                    boardFF[cni][cei]=-1;
                                    boardFF[csi][cwi]=-1;
                                    boardFF[csi][cei]=-1;
                                    tickets[h][w]= ticksSinceStart;
                                }
                                else
                                {
                                    headBuffer[h][w]=true;
                                }
                                boards[h][w]=Integer.valueOf(ls[1])+1;
                            }
                            if(ln.contains("die"))
                            {
                                l.info(ln);
                                String[] lns = ln.split("\\|");
                                for(int k=1;k<lns.length;k++)
                                {
                                    int death = Integer.valueOf(lns[k])+1;
                                    l.info("wiping"+death);
                                    for(int i=0;i<ch;i++) {
                                        for (int j = 0; j < cw; j++) {
                                            if(boards[i][j]==death)
                                            {
                                                boards[i][j]=0;
                                                tickets[i][j]=-ticksSinceStart;
                                            }
                                        }
                                    }
                                }

                            }
                            if(ln.contains("tick")&&gaming) {
                                heads_touched=0;
                                String boardview = "";
                                for(int i=0;i<ch;i++)
                                {
                                    for(int j=0;j<cw;j++)
                                    {

                                        int cni = i-1;
                                        if(cni==-1)
                                            cni=ch-1;
                                        int csi=(i+1)%ch;

                                        int cwi = j-1;
                                        if(cwi==-1)
                                            cwi=cw-1;
                                        int cei=(j+1)%cw;

                                        board[i][j]=0;
                                        board[i][j]+=(boards[i][j]!=0)?10:0;//||(boards[cni][j]!=0&&boards[csi][j]!=0)||(boards[i][cwi]!=0&&boards[j][cei]!=0));
                                        boardFF[i][j]=(boards[i][j]!=0)?-1:0;//||(boards[cni][j]!=0&&boards[csi][j]!=0)||(boards[i][cwi]!=0&&boards[j][cei]!=0));
                                        if(j==cx&&i==cy)
                                        {
                                            //boardview+="X";
                                        }
                                        //boardview+=boardFF[i][j]+";"+boards[i][j]+"|";
                                    }
                                    //boardview+="\n";
                                }
                                int cn = cy-1;
                                if(cn==-1)
                                    cn=ch-1;
                                int cs=(cy+1)%ch;

                                int cw_ = cx-1;
                                if(cw_==-1)
                                    cw_=cw-1;
                                int ce=(cx+1)%cw;
                                boolean randomDone = false;
                                String direction = "up";
                                // AAAAAAA
                                //l.info("\n"+boardview);
                                if(board[cn][cx]>9&&board[cs][cx]>9&&board[cy][ce]>9&&board[cy][cw_]>9)
                                {
                                    l.info("giving up?");
                                    randomDone=true;
                                    lastcase=3;
                                }
                                int west=0,east=0,south=0,north=0;
                                if(ticksSinceStart >0) {
                                    if(ticksSinceStart <0)
                                    {east = boardFF(ce, cy,1);
                                     west = boardFF(cw_, cy,2);

                                     north = boardFF(cx, cn,3);
                                     south = boardFF(cx, cs,4);
                                    }
                                    else
                                    {
                                        //int rnd = (int) (Math.random()*6);
                                        //int rnd=(fuck/13)%4;

                                        int rnd=lastDir;
                                        if(ticksSinceStart %100==0) returnMsgs.add("chat|All Hail the User!!!!!!");
                                        if(ticksSinceStart %100==50) returnMsgs.add("chat|Red 5 standing by");
                                        if(ticksSinceStart %100==75) returnMsgs.add("chat|rm -rf ~/botarmee*");
                                        l.info("lastdir"+lastDir);
                                        //boolean switcheroo = new File("/home/lh/switch").exists();

                                        switch(rnd)
                                        {
                                            case 3://prev=east
                                                if(switcheroo) south = boardFF(cx, cs,1);
                                                else north = boardFF(cx, cn,2);
                                                east = boardFF(ce, cy,3);
                                                if (switcheroo) north = boardFF(cx, cn,2);
                                                else south = boardFF(cx, cs,1);
                                                west = boardFF(cw_, cy,4);
                                                l.info("lnesw"+lastDir+";"+north+";"+east+";"+south+";"+west);

                                                break;
                                            case 1: //prev=west
                                                if(switcheroo) north = boardFF(cx, cn,1);
                                                else south = boardFF(cx, cs,2);
                                                west = boardFF(cw_, cy,3);
                                                if (switcheroo) south = boardFF(cx, cs,2);
                                                else north = boardFF(cx, cn,1);
                                                east = boardFF(ce, cy,4);
                                                break;
                                            case 2://prev=down
                                                if(switcheroo)west = boardFF(cw_, cy,1);
                                                else    east = boardFF(ce, cy,2);
                                                south = boardFF(cx, cs,3);
                                                if(switcheroo)east = boardFF(ce, cy,2);
                                                else west = boardFF(cw_, cy,1);
                                                north = boardFF(cx, cn, 4);
                                                break;
                                            case 0: //prev=up
                                                if(switcheroo)east = boardFF(ce, cy,1);
                                                else west = boardFF(cw_, cy,2);
                                                north = boardFF(cx, cn,3);
                                                if(switcheroo) west  =boardFF(cw_, cy,2);
                                                else east  =boardFF(ce, cy,1);
                                                south = boardFF(cx, cs,4);




                                                break;
                                        }
                                    }
                                }
                                else{
                                    if(ticksSinceStart <30) {
                                        south = boardFF(cx, cs,1);
                                        north = boardFF(cx, cn,2);

                                        west = boardFF(cw_, cy,3);
                                        east = boardFF(ce, cy,4);
                                    }

                                }
                                ticksSinceStart++;

                                int highest=Math.max(Math.max(north,south),Math.max(west,east));

                                l.info("hnesw"+highest+";"+north+";"+east+";"+south+";"+west);
                                if(((ticksSinceStart <20||(ticksSinceStart >40&& ticksSinceStart <60)||(500+ ticksSinceStart -330)%500<30)||(playercount<3&&(ticksSinceStart %100<50)))&&highest>1000)
                                {
                                    l.info(ticksSinceStart <20);
                                    l.info(((500+ ticksSinceStart -330)%500)<30);
                                    l.info("remappingparty");
                                    if(east==-1337)
                                        east=highest;
                                    if(west==-1337)
                                        west=highest;
                                    if(south==-1337)
                                        south=highest;
                                    if(north==-1337)
                                        north=highest;
                                }
                                else
                                {
                                    {
                                        if(east==-1337)
                                            east=0;
                                        if(west==-1337)
                                            west=0;
                                        if(south==-1337)
                                            south=0;
                                        if(north==-1337)
                                            north=0;
                                    }
                                }
                                if((highest<((cw*ch)/(playercount+1))||playercount<3))
                                {
                                    l.info("Switcheroo zone reached");

                                    if(ticksSinceStart %10==0)
                                    {
                                        l.info("chkFlick");
                                        if(heads_touched<2&&playercount>5&&ticksSinceStart>150)
                                        {
                                            l.info("headcount:"+heads_touched);
                                            switcheroo=!switcheroo;
                                        }
                                        if((ticksSinceStart /10)%3<2)
                                        {
                                            l.info("flick");
                                            switcheroo=!switcheroo;
                                        } {
                                            l.info("flick");
                                            switcheroo=!switcheroo;
                                        }
                                    }
                                }
                                l.info("switcheroo="+switcheroo);
                                l.info("fnesw"+ ticksSinceStart +";"+north+";"+east+";"+south+";"+west);
                                int rnd=(int)(Math.random()*4);
                                if(ticksSinceStart %300<25&& ticksSinceStart >15)
                                {
                                    rnd=0;
                                }
                                int lastchoice=-1;
                                int treshold=1;
                                while(!randomDone)
                                {
                                    //rnd = (int) (Math.random()*4);
                                    //rnd=lastchoice;
                                    /*lastchoice+=1;
                                    if(lastchoice>3)
                                    {
                                        lastchoice=0;
                                        treshold++;
                                    }
                                    if(treshold>3)
                                    {

                                    }*/
                                    //l.info("rnd="+rnd+"lastcse="+lastcase);
                                    /*if(rnd==lastcase)
                                    {
                                        l.info("reroll");
                                        continue;
                                    }*/
                                    if(highest<10)
                                    {
                                        returnMsgs.add("chat|godmayFUCKingdamnit");
                                    }
                                    switch(rnd)
                                    {
                                        case 1:
                                            //if((board[cy][cw_]<treshold))
                                            if(west==highest)
                                            {
                                                lastDir=1;
                                                debugdir="<";
                                                direction="left";
                                                randomDone=true;
                                                lastcase=3;}
                                            break;
                                        case 3:
                                            //if((board[cy][ce]<treshold))
                                            if(east==highest)
                                            {
                                                lastDir=3;
                                                direction="right";
                                                randomDone=true;
                                                debugdir=">";
                                                lastcase=1;
                                            }
                                            break;
                                        case 0:
                                            lastDir=0;
                                            //if(((board[cn][cx]*2)<treshold)){
                                            if(north==highest){
                                                debugdir="^";
                                                direction="up";
                                                randomDone=true;
                                                lastcase=2;}
                                            break;
                                        case 2:
                                            lastDir=2;
                                            //if((board[cs][cx]<treshold))
                                            if(south==highest)
                                            {
                                                debugdir="V";
                                                direction="down";
                                                 randomDone=true;
                                                 lastcase=0;}
                                            break;

                                    }
                                    rnd=(rnd+1)%4;
                                }
                                //headBuffer = new boolean[ch][cw];
                                l.trace("pc="+playercount);
                                playercount=0;
                                returnMsgs.add("move|"+direction);

                            }
                        }
                        else
                        {
                           throw new Error();
                        }

                    } catch (IOException ex) {
                        //l.warn("NETZAP");
                        return;
                        //break;
                    }
                    //l.trace(request);

                }
            });
            t.setName("InputCruncher");
            t.start();
            Thread t2 = new Thread(()->{
                //System.out.println("Gefangen!");
                while(sck.isConnected())
                {
                    synchronized(syncer)
                    {
                        //l.trace("MSG sent");
                        while(!returnMsgs.isEmpty())
                        {
                            // FUCK YOU DUMB IDIOT; NO THREAD.SLEEP() HERE!!!!!11!!11elf
                            String returnMsg = returnMsgs.poll();
                            if(returnMsg == null)
                            {
                                continue;
                            }
                            l.trace("PKT="+returnMsg);
                            out.print(returnMsg+'\n');
                            out.flush();
                        }
                        //l.trace("end of MessageStream");
                    }
                }
                //System.out.println("Flucht ist zwecklos");
            });
            t2.setName("OutputCruncher");
            t2.start();
            l.info("asdfqwxix");
        }
        catch (IOException ex)
    {
        ex.printStackTrace();
        l.error(" Unable to connect to Host. Aborting now");
        try {
            Thread.sleep(199);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    }
}
