package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

public class SimpleDhtProvider extends ContentProvider {

    static final String TAG = SimpleDhtProvider.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static final int SERVER_PORT = 10000;
    static int flg=0;
    static int starQueryCount=1;
    static int starDelCount=0;
    static int singleDelCount=0;
    static String mPort;
    static Boolean var=true;
    static boolean chkVal1=true;
    static boolean chkVal2=true;
    static ServerSocket serverSocket;
    HashMap<String, SucPred> nBorMap = new HashMap<>();
    static MatrixCursor cursor=new MatrixCursor(new String[]{"key", "value"});
    static HashMap<String,String> starQuery=new HashMap<String,String>();
    FileInputStream fis;
    String kvPair="";
    String readString = "";
    String[] cols1={"",""};
    Boolean cond;
    String cursSplit[]={"",""};
    File fileDir;
    File[] dirFiles;
    String fileSplit[];

    public static HashMap<String, String> starMap = new HashMap<String, String>();

    static final String[] portList = {REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4};


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub

        fileDir=new File(getContext().getApplicationContext().getFilesDir().toString());
        dirFiles = fileDir.listFiles();
        if (selection.equalsIgnoreCase("\"@\""))
        {


            int count=0;
//                String fileDir=getContext().getApplicationContext().getFilesDir().toString();
            if (dirFiles.length != 0) {
                // loops through the array of files, outputing the name to console
                MatrixCursor curs = new MatrixCursor(new String[]{"key", "value"});
                for (int ii = 0; ii < dirFiles.length; ii++) {
                    String fileOutput = dirFiles[ii].toString();

                    if(dirFiles[ii].delete())
                        count++;
                }

//                Log.d("cursor size:",""+curs.getCount());
                return count;
            }

            Log.v("directory in *", ""+getContext().getApplicationContext().getFilesDir());
        }
        else if(selection.equalsIgnoreCase("\"*\""))
        {
            if (dirFiles.length != 0) {
                // loops through the array of files, outputing the name to console
                MatrixCursor curs = new MatrixCursor(new String[]{"key", "value"});
                for (int ii = 0; ii < dirFiles.length; ii++) {
                    String fileOutput = dirFiles[ii].toString();

                    if(dirFiles[ii].delete())
                        starDelCount++;
                }


//                Log.d("cursor size:",""+curs.getCount());
//                return count;
            }

            try {
                return initDel(starDelCount,mPort,nBorMap.get(mPort).getSuc(),"me");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else
        {


            Log.d("new msg","New Single Message:"+selection);
            cursor=new MatrixCursor(new String[]{"key", "value"});


            //Working Code for single Query

            //**************************************STARTS HERE**************************************************
            Log.d("new Single","New Single message");
            flg=0;
//                File fileDir=new File(getContext().getApplicationContext().getFilesDir().toString());
//                File[] dirFiles = fileDir.listFiles();
//                String fileSplit[];
            String fName="";

            File file=null;
            String keyValPair="";

            if (dirFiles.length != 0) {
                for (int ii = 0; ii < dirFiles.length; ii++) {
                    String fileOutput = dirFiles[ii].toString();
                    fileSplit = fileOutput.split("/");
                    if(selection.equalsIgnoreCase(fileSplit[fileSplit.length-1]))
                    {
                        flg=1;
                        file=dirFiles[ii];
                        fName=fileSplit[fileSplit.length-1];
                        break;
                    }

                }
            }


            Log.d("flag val","flag is:"+flg);
            if(flg==1) {
                Log.v("selection is:", "" + selection);
                if(file.delete())
                    singleDelCount++;
            }


            try {
                return initDelSingle(singleDelCount,mPort,nBorMap.get(mPort).getSuc(),selection);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //*************************************ENDS HERE*******************************************************

        }


        return 0;
    }


    public int initDelSingle(int curs,String origPort,String sucPort,String selection) throws InterruptedException {
//        MatrixCursor cursor=new MatrixCursor(new String[]{"key", "value"});

//        var=true;
//        if(sucPort.equalsIgnoreCase("null") || sucPort==null)
//            var=false;

//        if(mPort.equalsIgnoreCase(origPort))
//            var=false;
        Log.d("key value pair:","key value pair being passed:"+curs+",succ port:"+sucPort);
        if(curs>0 && mPort.equalsIgnoreCase(origPort))
        {
            Log.d("cond satisfied","contains comma and mport now original and var is:"+var);
            return curs;
//            return cursor;

        }
        else
        {
            Log.d("else?","coming in else");
            var=true;

            new ClientTaskDel().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,""+curs, origPort,sucPort,selection);

        }

        Thread.sleep(1200);


        return singleDelCount;
    }

    private class ClientTaskDel extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {

                Log.d("coming in CT1","coming in client task");
                int curs=Integer.parseInt(msgs[0]);
                String origPort=msgs[1];
                String sucPort=msgs[2];
                String selection=msgs[3];

                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(sucPort));

                SingleDelete cObj=new SingleDelete();
                cObj.setOrigPort(origPort);
                cObj.setCurs(curs);
                cObj.setThisPort(sucPort);
                cObj.setStat(selection);
                ObjectOutputStream msgObject = new ObjectOutputStream(socket.getOutputStream());
                msgObject.writeObject(cObj);
                msgObject.close();
                socket.close();

                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
//                socket.close();

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }


    public int initDel(int cnt,String origPort,String sucPort,String stat) throws InterruptedException {

//        Boolean var=true;

        Log.d("stat:","stat:"+stat+",mPort:"+mPort+",origPort:"+origPort);

        Log.d("in init","coming in init funct after * with mPort:"+mPort+" and origPort:"+origPort+" cursor size:"+cursor.getCount());
        if(sucPort.equalsIgnoreCase("null") || sucPort.equalsIgnoreCase("0"))
        {
            return cnt;
        }
//        MatrixCursor curs1=curs;
//        if(sucPort.equalsIgnoreCase("null") || sucPort==null)
//            var=false;
        else if(mPort.equalsIgnoreCase(origPort)) {

            Log.d("mp==op","in init mPort=OrigPort"+" and count:"+cnt+" and stat:"+stat);
            if(stat.equalsIgnoreCase("me"))
            {
                Log.d("forward","forward from init to succ port:"+sucPort);

                delFiles(cnt,origPort,sucPort,stat);

            }
            else if(stat.equalsIgnoreCase("notme"))
            {
                return cnt;
            }

            var = false;
        }
        else
        {
            Log.d("forward","forward from init to succ port:"+sucPort);
            delFiles(cnt,origPort,sucPort,stat);
        }

//        while(var)
//        {
//
//        }
        Thread.sleep(1200);

//        return curs1;

        return cnt;
    }

    public void delFiles(int curs,String origPort,String sucPort,String stat)
    {
        try {
            Log.d("fwd","from init to be forwarded to:"+sucPort);
            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    Integer.parseInt(sucPort));

            GlobalDelete cObj=new GlobalDelete();
            cObj.setOrigPort(origPort);
            cObj.setCurs(curs);
            cObj.setThisPort(sucPort);
            cObj.setStat(stat);
            ObjectOutputStream msgObject = new ObjectOutputStream(socket.getOutputStream());

            msgObject.writeObject(cObj);
            msgObject.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        Log.d("checking insert", "cheking insert function in:" + mPort + " and Map value:" + nBorMap.get(mPort).getNid() + ",succ:" + nBorMap.get(mPort).getSuc());
        String key = values.getAsString("key");
        String value = values.getAsString("value");
        Log.d("KeyValPair", key + "," + value);
        try {
            if (genHash(key).compareTo(genHash("" + Integer.parseInt(mPort) / 2)) > 0) {
                Log.d("hash ch", "hash of msg greater than port");
                Log.d("to suc", "So now it will go to successor:" + nBorMap.get(mPort).getSuc() + ",size:" + nBorMap.size());

                if (nBorMap.get(mPort).getSuc().equalsIgnoreCase("null") || nBorMap.get(mPort).getSuc().equalsIgnoreCase("0")) {
                    Log.d("yes Null", "yes Suc is null");
                    FileOutputStream fOut = getContext().openFileOutput(key, Context.MODE_PRIVATE);
                    fOut.write(value.getBytes());
                    fOut.close();

                } else {
                    sendInsertSucc(nBorMap.get(mPort).getSuc(), key + "," + value, mPort);
//                    new ClientTask3().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, key+","+value, mainMap.get(mPort).getSuc());
                }

            } else if (genHash(key).compareTo(genHash("" + Integer.parseInt(mPort) / 2)) < 0) {
                Log.d("hash ch", "hash of msg lesser than port");
                Log.d("to pred", "So now it will go to its pred:" + nBorMap.get(mPort).getPred());


                if (nBorMap.get(mPort).getPred().equalsIgnoreCase("null") || nBorMap.get(mPort).getPred().equalsIgnoreCase("0")) {
                    Log.d("yes Null", "yes pred is null");
                    FileOutputStream fOut = getContext().openFileOutput(key, Context.MODE_PRIVATE);
                    fOut.write(value.getBytes());
                    fOut.close();

                } else if (genHash("" + Integer.parseInt(mPort) / 2).compareTo(genHash("" + Integer.parseInt(nBorMap.get(mPort).getPred()) / 2)) < 0) {
                    FileOutputStream fOut = getContext().openFileOutput(key, Context.MODE_PRIVATE);
                    fOut.write(value.getBytes());
                    fOut.close();
                } else if (genHash(key).compareTo(genHash("" + Integer.parseInt(nBorMap.get(mPort).getPred()) / 2)) > 0) {
                    Log.d("stored here", "getting stored here as its greater than pred");
                    FileOutputStream fOut = getContext().openFileOutput(key, Context.MODE_PRIVATE);
                    fOut.write(value.getBytes());
                    fOut.close();
                } else {

                    Log.d("ToDo", "Send to pred here");
                    sendInsert(nBorMap.get(mPort).getPred(), key + "," + value, mPort);
//                    new ClientTask3().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, key+","+value, mainMap.get(mPort).getSuc());
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        var=true;
        mPort = myPort;
        String suc = "null";
        String pred = "null";
        SucPred firstAvd = new SucPred();
        firstAvd.setNid(mPort);
        firstAvd.setSuc(suc);
        firstAvd.setPred(pred);
        Log.d("in oncreate", "oncreate");
        nBorMap.put(mPort, firstAvd);
        cond = true;

        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");

        }


        String msg = myPort;
        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
        return false;


    }


    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        int count = 0, countRing = 0;//it stays static inside instanceOf Message condition

        String suc = "0", pred = "0";
        HashMap<String, SucPred> nodeMap = new HashMap<>();
        String[] kList;
        Map<String, SucPred> tMap;
        String keyConcat = "";
        int i = 0;

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            String message = "";
            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
            String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
            fileDir=new File(getContext().getApplicationContext().getFilesDir().toString());
            dirFiles = fileDir.listFiles();
//            fileSplit[];
//            mPort=myPort;
            Uri mUri;
            mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
            Log.d("checking cond","cond value:");
            //String key="key";

            try {
                while (true) {
                    Log.d("coming in while?","coming in while");
                    String uri = "edu.buffalo.cse.cse486586.simpledht.provider";
                    Socket socket = serverSocket.accept();
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    Object recdObj = ois.readObject();
                    if (recdObj instanceof Message) {
                        count++;
                        Message mObj = (Message) recdObj;
                        String MessageHash = genHash("" + (Integer.parseInt(mObj.getMsg()) / 2) + "");
                        if (suc.equalsIgnoreCase("0") && pred.equalsIgnoreCase("0") && (MessageHash.compareTo(genHash("" + Integer.parseInt(mPort) / 2 + "")) == 0)) {

                            suc = "0";
                            pred = "0";
                            SucPred spObj = new SucPred();
                            spObj.setNid(mPort);
                            spObj.setSuc(suc);
                            spObj.setPred(pred);
                            nodeMap.put(genHash("" + Integer.parseInt(mPort) / 2), spObj);
                            tMap = new TreeMap<String, SucPred>(nodeMap);
//                            sendInform(myPort,suc,pred);
                            Log.d("Messg recvd", mObj.getMsg() + ",with count:" + count + ",on Orig Port:" + mPort);
                            Log.d("sucPred filled:", "populated with" + suc + "," + pred);
                        } else if (suc.equalsIgnoreCase("0") && pred.equalsIgnoreCase("0") && (MessageHash.compareTo(genHash("" + Integer.parseInt(mPort) / 2 + "")) != 0)) {
                            suc = "1";
                            pred = "1";
                            SucPred s1 = new SucPred();
                            s1.setNid(mPort);
                            s1.setSuc(mObj.getMsg());
                            s1.setPred(mObj.getMsg());

                            SucPred s2 = new SucPred();
                            s2.setNid(mObj.getMsg());
                            s2.setSuc(mPort);
                            s2.setPred(mPort);

                            nodeMap.put(genHash("" + Integer.parseInt(mPort) / 2), s1);
                            nodeMap.put(genHash("" + Integer.parseInt(mObj.getMsg()) / 2), s2);

                            Log.d("bfor", "map before sort");
                            for (Map.Entry<String, SucPred> ent : nodeMap.entrySet()) {
                                Log.d("entry", ent.getKey() + "," + ent.getValue().getNid() + "," + ent.getValue().getSuc() + "," + ent.getValue().getPred());
                            }

                            tMap = new TreeMap<String, SucPred>(nodeMap);
                            Log.d("aftr", "map after sort");
                            for (Map.Entry<String, SucPred> ent : tMap.entrySet()) {
                                Log.d("entry", ent.getKey() + "," + ent.getValue().getNid() + "," + ent.getValue().getSuc() + "," + ent.getValue().getPred());
                            }


//                            sendInform(myPort,mObj.getMsg(),mObj.getMsg());
//                            sendInform(mObj.getMsg(),myPort,myPort);

                            Log.d("Messg recvd", mObj.getMsg() + ",with count:" + count + ",on Orig Port:" + mPort);
                            Log.d("sucPred pplated:", suc + "," + pred);
                        } else {
                            SucPred s = new SucPred();
                            s.setNid(mObj.getMsg());
                            nodeMap.put(genHash("" + Integer.parseInt(mObj.getMsg()) / 2), s);
                            Log.d("bfor", "map before sort");
                            for (Map.Entry<String, SucPred> ent : nodeMap.entrySet()) {
                                Log.d("entry", ent.getKey() + "," + ent.getValue().getNid() + "," + ent.getValue().getSuc() + "," + ent.getValue().getPred());
                            }

                            tMap = new TreeMap<String, SucPred>(nodeMap);
                            Log.d("aftr", "map after sort");
                            for (Map.Entry<String, SucPred> ent : tMap.entrySet()) {
                                Log.d("entry", ent.getKey() + "," + ent.getValue().getNid() + "," + ent.getValue().getSuc() + "," + ent.getValue().getPred());
                            }
                        }
                        kList = new String[tMap.size()];
                        i = 0;
                        for (Map.Entry<String, SucPred> ent : tMap.entrySet()) {
                            kList[i] = ent.getValue().getNid();
                            i++;
//                               sendInform(ent.getValue().getNid(),ent.getValue().getSuc(),ent.getValue().getPred());
                        }

                        i = 0;
                        if (kList.length == 1) {
                            sendInform(kList[0], "0", "0");
                        } else {
                            for (i = 0; i < kList.length; i++) {
                                if (i == 0) {
                                    sendInform(kList[i], kList[i + 1], kList[kList.length - 1]);
                                } else if (i == kList.length - 1) {
                                    sendInform(kList[i], kList[0], kList[i - 1]);
                                } else if (i > 0 && i < kList.length - 1) {
                                    sendInform(kList[i], kList[i + 1], kList[i - 1]);
                                }
                            }
                        }
                        //DO NOT DELETE
                    }


                    if (recdObj instanceof Test) {
                        Test obj = (Test) recdObj;
                        String port = obj.getMsg().split(",")[0];
                        String s = obj.getMsg().split(",")[1];
                        String p = obj.getMsg().split(",")[2];
                        SucPred sp = new SucPred();
                        sp.setNid(port);
                        sp.setSuc(s);
                        sp.setPred(p);
                        Log.d("Conveyed Suc Pred", port + "'s suc is:" + s + " and pred is:" + p);
                        nBorMap.put(port, sp);
//                        ContentValues cv=new ContentValues();
//                        Uri Uri;
//                        Uri=buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
//                        insert(Uri,cv);
                        //DO NOT DELETE
                    }

                    if (recdObj instanceof Msg1Insert){

                        Msg1Insert m2I = (Msg1Insert) recdObj;
                        Log.d("coming in Server Task", "Insert message coming in server task:" + m2I.getKey());
                        if (genHash(m2I.getKey()).compareTo(genHash("" + Integer.parseInt(m2I.getPort()) / 2)) < 0) {

                            Log.d("succ se chota", "succ se chota");
                            FileOutputStream fOut = getContext().openFileOutput(m2I.getKey(), Context.MODE_PRIVATE);
                            fOut.write(m2I.getMsg().getBytes());
                            fOut.close();
                        } else if (m2I.getPort().equalsIgnoreCase(m2I.getOrigPort())) {
                            Log.d("circle completed", "inserting in original port as circle completed");
                            FileOutputStream fOut = getContext().openFileOutput(m2I.getKey(), Context.MODE_PRIVATE);
                            fOut.write(m2I.getMsg().getBytes());
                            fOut.close();
                        } else if (genHash("" + Integer.parseInt(m2I.getPort()) / 2).compareTo(genHash("" + Integer.parseInt(nBorMap.get(m2I.getPort()).getPred()) / 2)) < 0) {
                            FileOutputStream fOut = getContext().openFileOutput(m2I.getKey(), Context.MODE_PRIVATE);
                            fOut.write(m2I.getMsg().getBytes());
                            fOut.close();
                        } else if (genHash("" + Integer.parseInt(m2I.getPort()) / 2).compareTo(genHash("" + Integer.parseInt(nBorMap.get(m2I.getPort()).getSuc()) / 2)) > 0) {
                            Log.d("Greatest Corner case", "Corner case where node with least hash is succesor ");
                            sendInsertSucc(nBorMap.get(m2I.getPort()).getSuc(), m2I.getKey() + "," + m2I.getMsg(), nBorMap.get(m2I.getPort()).getSuc());
                        } else {
                            Log.d("this case", "this is where there is problem");
                            sendInsertSucc(nBorMap.get(m2I.getPort()).getSuc(), m2I.getKey() + "," + m2I.getMsg(), m2I.getOrigPort());
                        }
                        //DO NOT DELETE
                    }

                    if (recdObj instanceof Mesg2Insert){
                        Mesg2Insert m2I = (Mesg2Insert) recdObj;
                        Log.d("coming in Server Task", "Insert message coming in server task:" + m2I.getKey());
                        if (genHash(m2I.getKey()).compareTo(genHash("" + Integer.parseInt(nBorMap.get(m2I.getPort()).getPred()) / 2)) > 0) {
                            Log.d("pred se bada", "bigger than pred");

                            FileOutputStream fOut = getContext().openFileOutput(m2I.getKey(), Context.MODE_PRIVATE);
                            fOut.write(m2I.getMsg().getBytes());
                            fOut.close();

                        } else if (genHash("" + Integer.parseInt(m2I.getPort()) / 2).compareTo(genHash("" + Integer.parseInt(nBorMap.get(m2I.getPort()).getPred()) / 2)) < 0) {
                            Log.d("Least Corner case", "Corner case where node with Greatest hash is predecessor ");
                            FileOutputStream fOut = getContext().openFileOutput(m2I.getKey(), Context.MODE_PRIVATE);
                            fOut.write(m2I.getMsg().getBytes());
                            fOut.close();
                        }



                        else {
                            sendInsert(nBorMap.get(m2I.getPort()).getPred(), m2I.key + "," + m2I.getMsg(), m2I.getOrigPort());
                        }
                        //DO NOT DELETE
                    }


                    if(recdObj instanceof QueryClass1)
                    {
                        QueryClass1 m2I = (QueryClass1) recdObj;
                        String sel=m2I.getMsg();
                        Log.d("coming in Server Task", "Query message coming in server task:" + m2I.getMsg());


                        if (genHash(m2I.getMsg()).compareTo(genHash("" + Integer.parseInt(m2I.getPort()) / 2)) < 0) {

                            Log.d("succ se chota", "succ se chota");
                            //here itself

                            fis = getContext().openFileInput(sel);
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader buffreader = new BufferedReader(isr);
                            readString = buffreader.readLine();
                            kvPair=sel+","+readString;
                            Log.d("file:",sel+",value:"+readString);
//                                isr.close();
//                                fis.close();
                            Log.d("passing","Passing  to origPort from:"+mPort);
//                                cursor.addRow(new String[]{sel,readString});

                            addRecQC2(m2I.getOrigPort(),m2I.getMsg(), m2I.getOrigPort(),kvPair);

                        } else if (m2I.getPort().equalsIgnoreCase(m2I.getOrigPort()) ) {
                            if(!m2I.getKvp().contains(",")) {
                                Log.d("circle completed", "Querying in original port as circle completed");
                                //here itself
                                fis = getContext().openFileInput(sel);
                                InputStreamReader isr = new InputStreamReader(fis);
                                BufferedReader buffreader = new BufferedReader(isr);
                                readString = buffreader.readLine();
                                kvPair = sel + "," + readString;
                                Log.d("file:", sel);
//                                    isr.close();
//                                    fis.close();
                                cursor.addRow(new String[]{sel,readString});
                                chkVal2=false;
                            }
                            else
                            {
                                kvPair=m2I.getKvp();
                                Log.d("passing","Passing  to origPort from:"+mPort);
                                addRecQC2(m2I.getOrigPort(),m2I.getMsg(), m2I.getOrigPort(),kvPair);
                            }


                        }

                        else if (genHash("" + Integer.parseInt(m2I.getPort()) / 2).compareTo(genHash("" + Integer.parseInt(nBorMap.get(m2I.getPort()).getPred()) / 2)) < 0) {
                            //Here itself
                            fis = getContext().openFileInput(sel);
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader buffreader = new BufferedReader(isr);
                            readString = buffreader.readLine();
                            kvPair=sel+","+readString;
                            Log.d("file:",sel);
//                                isr.close();
//                                fis.close();
                            Log.d("passing","Passing  to origPort from:"+mPort);
                            addRecQC2(m2I.getOrigPort(),m2I.getMsg(), m2I.getOrigPort(),kvPair);

                        } else if (genHash("" + Integer.parseInt(m2I.getPort()) / 2).compareTo(genHash("" + Integer.parseInt(nBorMap.get(m2I.getPort()).getSuc()) / 2)) > 0) {
                            Log.d("Greatest Corner case", "Corner case where node with least hash is succesor ");
//                                queryInsertSucc(nBorMap.get(m2I.getPort()).getSuc(), m2I.getMsg(), nBorMap.get(m2I.getPort()).getSuc());
                            Log.d("passing","Passing  to successor"+ nBorMap.get(m2I.getPort()).getSuc()+"from:"+mPort);
                            addRecQC2(nBorMap.get(m2I.getPort()).getSuc(),m2I.getMsg(), m2I.getOrigPort(),kvPair);
                        } else {
                            Log.d("Query case", "this is where there is problem");
//                                queryInsertSucc(nBorMap.get(m2I.getPort()).getSuc(),m2I.getMsg(), m2I.getOrigPort());
                            Log.d("passing","Passing  to successor"+ nBorMap.get(m2I.getPort()).getSuc()+"from:"+mPort);
                            addRecQC2(nBorMap.get(m2I.getPort()).getSuc(),m2I.getMsg(), m2I.getOrigPort(),kvPair);
                        }

                    }

                    if(recdObj instanceof QueryClass2)
                    {
                        QueryClass2 m2I = (QueryClass2) recdObj;
                        String sel=m2I.getMsg();
                        Log.d("coming in Server Task", "Query message coming in server task:" + m2I.getMsg());
                        if (genHash(m2I.getMsg()).compareTo(genHash("" + Integer.parseInt(nBorMap.get(m2I.getPort()).getPred()) / 2)) > 0) {
                            Log.d("pred se bada", "bigger than pred");

                            //here itself
                            fis = getContext().openFileInput(sel);
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader buffreader = new BufferedReader(isr);
                            readString = buffreader.readLine();
                            kvPair=sel+","+readString;
                            Log.d("file:",sel);
//                            isr.close();
//                            fis.close();
                            Log.d("passing","Passing  to origPort from:"+mPort);
                            addRecQC1(m2I.getOrigPort(),m2I.getMsg(), m2I.getOrigPort(),kvPair);


                        } else if (genHash("" + Integer.parseInt(m2I.getPort()) / 2).compareTo(genHash("" + Integer.parseInt(nBorMap.get(m2I.getPort()).getPred()) / 2)) < 0) {
                            Log.d("Least Corner case", "Corner case where node with Greatest hash is predecessor ");
                            //Here itself
                            fis = getContext().openFileInput(sel);
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader buffreader = new BufferedReader(isr);
                            readString = buffreader.readLine();
                            kvPair=sel+","+readString;
                            Log.d("file:",sel);
//                            isr.close();
//                            fis.close();
                            Log.d("passing","Passing  to origPort from:"+mPort);
                            addRecQC1(m2I.getOrigPort(),m2I.getMsg(), m2I.getOrigPort(),kvPair);

                        }



                        else {
//                            queryInsert(nBorMap.get(m2I.getPort()).getPred(),m2I.getMsg(), m2I.getOrigPort());
                            Log.d("passing","Passing  to Predecessor"+ nBorMap.get(m2I.getPort()).getPred()+"from:"+mPort);
                            addRecQC1(nBorMap.get(m2I.getPort()).getPred(),m2I.getMsg(), m2I.getOrigPort(),kvPair);
                        }

                    }




                    if(recdObj instanceof CursorClass)
                    {
                        starQueryCount++;
                        Log.d("starQTime","startQueryCound in ST:"+starQueryCount);

                        CursorClass cObj=(CursorClass)recdObj;
                        String origPort=cObj.getOrigPort();
                        String thisPort=cObj.getThisPort();
                        Log.d("came","came to ServerTask from init now mPort:"+mPort+",this Port:"+thisPort+" origPort:"+origPort);
                        HashMap<String,String> rcvCurs=cObj.getCurs();
                        FileInputStream fis;
                        String readString = "";
                        String[] cols1={"",""};

//                        MatrixCursor curs = new MatrixCursor(new String[]{"key", "value"});

                        File fileDir=new File(getContext().getApplicationContext().getFilesDir().toString());
                        File[] dirFiles = fileDir.listFiles();
                        String fileSplit[];
                        if (dirFiles.length != 0)
                        {
                            for (int ii = 0; ii < dirFiles.length; ii++)
                            {
                                String fileOutput = dirFiles[ii].toString();
                                fileSplit = fileOutput.split("/");
                                try
                                {
                                    fis = getContext().openFileInput(fileSplit[fileSplit.length - 1]);
                                    InputStreamReader isr = new InputStreamReader(fis);
                                    BufferedReader buffreader = new BufferedReader(isr);
                                    readString = buffreader.readLine();
//                                    rcvCurs.addRow(new String[]{fileSplit[fileSplit.length - 1], readString});
                                    if(!rcvCurs.containsKey(fileSplit[fileSplit.length - 1]))
                                        rcvCurs.put(fileSplit[fileSplit.length - 1],readString);

                                    Log.d("file:", fileSplit[fileSplit.length - 1]);
                                    isr.close();
                                    fis.close();

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }


//                            MatrixCursor cs=mergeCursor;


                            Log.d("cursor size:", "Cursor size in Server Task:" + rcvCurs.size());
                            Log.d("My succ in Query:", "" + nBorMap.get(mPort).getSuc());

                        }

                        init(rcvCurs,origPort,nBorMap.get(thisPort).getSuc(),"notme");
                    }

                    if(recdObj instanceof GlobalDelete)
                    {
                        GlobalDelete dg=(GlobalDelete)recdObj;
                        String thisPort=dg.getThisPort();
                        String origPort=dg.getOrigPort();
                        File fileDir=new File(getContext().getApplicationContext().getFilesDir().toString());
                        File[] dirFiles = fileDir.listFiles();
                        String fileSplit[];
                        if (dirFiles.length != 0)
                        {
                            for (int ii = 0; ii < dirFiles.length; ii++)
                            {
                                if(dirFiles[ii].delete())
                                    starDelCount++;
                            }


//                            MatrixCursor cs=mergeCursor;


//                            Log.d("cursor size:", "Cursor size in Server Task:" + rcvCurs.size());
                            Log.d("My succ in Query:", "" + nBorMap.get(mPort).getSuc());

                        }

                        initDel(starDelCount, origPort, nBorMap.get(thisPort).getSuc(), "notme");
                    }


                    if(recdObj instanceof SingleDelete)
                    {
//                        cursor=new MatrixCursor(new String[]{"key", "value"});


                        Log.d("forwarded Single Query:","forwarded to port:"+mPort);
                        SingleDelete sObj=(SingleDelete)recdObj;
                        String origPort=sObj.getOrigPort();
                        String thisPort=sObj.getThisPort();
                        String selection=sObj.getStat();
                        int rcvCurs=sObj.getCurs();
                        File fileDir=new File(getContext().getApplicationContext().getFilesDir().toString());
                        File[] dirFiles = fileDir.listFiles();
                        String fileSplit[];

                        FileInputStream fis;
                        String readString = "";
                        String[] cols1={"",""};
                        String fName="";

                        flg=0;
                        File file=null;

                        if (dirFiles.length != 0) {
                            for (int ii = 0; ii < dirFiles.length; ii++) {
                                String fileOutput = dirFiles[ii].toString();
                                fileSplit = fileOutput.split("/");
                                if(selection.equalsIgnoreCase(fileSplit[fileSplit.length-1]))
                                {
                                    flg=1;
                                    file=dirFiles[ii];
                                    fName=fileSplit[fileSplit.length-1];
//                                    break;
                                }

                            }
                        }

                        Log.d("flag val","flag is:"+flg);
                        if(flg==1) {
                            Log.v("selection is:", "" + selection +" in "+mPort);
                            if(file.delete())
                                rcvCurs++;
                        }

//                        else if(flg==0)
//                        {
//                            rcvCurs=null;
//                        }
                        if(mPort.equalsIgnoreCase(sObj.getOrigPort()))
                        {
                            Log.d("check","cond satisfied in Servertask");
//                            cursor = new MatrixCursor(new String[]{"key","value"});
//                            cursor.addRow(new String[]{selection,readString});
                            var=false;
                        }
//                        else
                        initDelSingle(rcvCurs, origPort, nBorMap.get(thisPort).getSuc(), selection);


                    }


                    if(recdObj instanceof SingleRecord)
                    {
//                        cursor=new MatrixCursor(new String[]{"key", "value"});


                        Log.d("forwarded Single Query:","forwarded to port:"+mPort);
                        SingleRecord sObj=(SingleRecord)recdObj;
                        String origPort=sObj.getOrigPort();
                        String thisPort=sObj.getThisPort();
                        String selection=sObj.getSelection();
                        String rcvCurs=sObj.getCurs();
                        File fileDir=new File(getContext().getApplicationContext().getFilesDir().toString());
                        File[] dirFiles = fileDir.listFiles();
                        String fileSplit[];

                        FileInputStream fis;
                        String readString = "";
                        String[] cols1={"",""};
                        String fName="";

                        flg=0;
                        if (dirFiles.length != 0) {
                            for (int ii = 0; ii < dirFiles.length; ii++) {
                                String fileOutput = dirFiles[ii].toString();
                                fileSplit = fileOutput.split("/");
                                if(selection.equalsIgnoreCase(fileSplit[fileSplit.length-1]))
                                {
                                    flg=1;
                                    Log.d("file found","falg 1 becox file found");
                                    fName=fileSplit[fileSplit.length-1];
//                                    break;
                                }

                            }
                        }

                        Log.d("flag val","flag is:"+flg);
                        if(flg==1) {
                            Log.v("selection is:", "" + selection +" in "+mPort);
                            fis = getContext().openFileInput(fName);
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader buffreader = new BufferedReader(isr);
                            readString = buffreader.readLine();
                            Log.v("query res:", "" + readString +" in "+mPort);
                            cols1[0] = selection;
                            cols1[1] = readString;
                            rcvCurs=selection+","+readString;
                            starMap.put(selection, readString);
                            isr.close();
                            fis.close();
                        }

//                        else if(flg==0)
//                        {
//                            rcvCurs=null;
//                        }
                        if(mPort.equalsIgnoreCase(sObj.getOrigPort()))
                        {
                            Log.d("check","cond satisfied in Servertask");
//                            cursor = new MatrixCursor(new String[]{"key","value"});
//                            cursor.addRow(new String[]{selection,readString});
                            var=false;
                        }
//                        else
                        initSingle(rcvCurs,origPort,nBorMap.get(thisPort).getSuc(),selection);


                    }
//                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        private Uri buildUri(String scheme, String authority) {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
            //DO NOT DELETE
        }
    }

    public void sendInsert(String port, String info, String origPort) {
        Log.d("Try", "Send to Pred");
        try {
            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    Integer.parseInt(port));

            String msgSplit[] = info.split(",");
            Mesg2Insert tObj = new Mesg2Insert();
            tObj.setKey(msgSplit[0]);
            tObj.setMsg(msgSplit[1]);
            tObj.setPort(port);
            tObj.setOrigPort(origPort);

            ObjectOutputStream spObject = new ObjectOutputStream(socket.getOutputStream());
            spObject.writeObject(tObj);


            spObject.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInsertSucc(String port, String info, String origPort) {
        Log.d("bigger", "forward to succ");
        try {
            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    Integer.parseInt(port));

            String msgSplit[] = info.split(",");
            Msg1Insert tObj = new Msg1Insert();
            tObj.setKey(msgSplit[0]);
            tObj.setMsg(msgSplit[1]);
            tObj.setPort(port);
            tObj.setOrigPort(origPort);

            ObjectOutputStream spObject = new ObjectOutputStream(socket.getOutputStream());
            spObject.writeObject(tObj);


            spObject.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendInform(String port, String successor, String predecessor) {
        Socket socket = null;
        try {
            socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    Integer.parseInt(port));
            Test tObj = new Test();
            tObj.setMsg(port + "," + successor + "," + predecessor);

            ObjectOutputStream spObject = new ObjectOutputStream(socket.getOutputStream());
            spObject.writeObject(tObj);


            spObject.close();
            socket.close();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void send(String str, String flag) {
        try {

            if (flag.equalsIgnoreCase("inform")) {
                if (str.length() == 6) {
                    String suc = "", pred = "";
                    Log.d("length 6", "Length is 6 now:" + str);
                    String finStr = str.substring(1, str.length());
                    Log.d("Trunc", "Length is now:" + finStr);
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(finStr));

                    //TODO
                    suc = "0";
                    pred = "0";

                    Test tObj = new Test();
                    tObj.setMsg(finStr + "," + suc + "," + pred);

                    ObjectOutputStream spObject = new ObjectOutputStream(socket.getOutputStream());
                    spObject.writeObject(tObj);


                    spObject.close();
                    socket.close();

                } else if (str.length() > 6) {
                    String keyList = str.substring(1, str.length());

                    String keys[] = keyList.split(",");
                    Log.d("send function", "coming here?");
                    int i = 0;
                    String suc = "", pred = "";

                    if (keys.length == 2) {
                        for (i = 0; i < keys.length; i++) {
                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(keys[i]));

                            if (i == 0) {
                                pred = keys[i + 1];
                                suc = keys[i + 1];
                            } else if (i == keys.length - 1) {
                                suc = keys[i - 1];
                                pred = keys[i - 1];
                            }

                            Test tObj = new Test();
                            tObj.setMsg(keys[i] + "," + suc + "," + pred);

                            ObjectOutputStream spObject = new ObjectOutputStream(socket.getOutputStream());
                            spObject.writeObject(tObj);


                            spObject.close();
                            socket.close();
                        }

                    } else if (keys.length > 2) {
                        for (i = 0; i < keys.length; i++) {
                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(keys[i]));

                            if (i == 0) {
                                pred = keys[keys.length - 1];
                                suc = keys[i + 1];
                            } else if (i == keys.length - 1) {
                                suc = keys[0];
                                pred = keys[i - 1];
                            } else if (i > 0 && i < keys.length - 1) {
                                suc = keys[i + 1];
                                pred = keys[i - 1];
                            }

                            Test tObj = new Test();
                            tObj.setMsg(keys[i] + "," + suc + "," + pred);

                            ObjectOutputStream spObject = new ObjectOutputStream(socket.getOutputStream());
                            spObject.writeObject(tObj);


                            spObject.close();
                            socket.close();
                        }
                    }
                }
            }

//            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {


                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt("11108"));

                String msgToSend = msgs[0];
                Message msg = new Message();
                msg.setMsg(msgToSend);
                ObjectOutputStream msgObject = new ObjectOutputStream(socket.getOutputStream());
                msgObject.writeObject(msg);


                msgObject.close();
                socket.close();
                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                socket.close();

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }


    public MatrixCursor addRecQC1(String predP,String select,String orgP,String res)
    {
        MatrixCursor cs=null;

        Log.d("receiving","getting in addRecQC1:"+predP+",mPort:"+mPort+",origP:"+orgP+",res:"+res+",chkVal1:"+chkVal1);
        if(!res.contains(","))
        {
            Log.d("not yet","no comma yet");
            queryInsert(predP,select,orgP,res);
        }
        else
        {
            Log.d("yes","yes we got and now forwarding from addRecQC1");
            if(mPort.equalsIgnoreCase(orgP)) {
                String resSplit[] = res.split(",");
                cursor.addRow(new String[]{resSplit[0], resSplit[1]});
                chkVal1 = false;
                return cursor;
            }
            else
            {
                Log.d("Not OrigP","this is not Original port so forwarding to Origport");
                queryInsert(orgP, select, orgP, res);
            }
        }


        while(chkVal1)
        {

        }

//        chkVal1=true;
        return cursor;
    }
    public MatrixCursor addRecQC2(String sucP,String select,String orgP,String res)
    {
        MatrixCursor cs=null;
        Log.d("receiving","getting in addRecQC2 of:"+sucP+",mPort:"+mPort+",orgP:"+orgP+" chkVal2:"+chkVal2+" res is:"+res);

        if(!res.contains(","))
        {
            Log.d("not yet","no comma yet");
            queryInsertSucc(sucP,select,orgP,res);
        }
        else
        {
            Log.d("yes","yes we got and now forwarding from addRecQC2");

            if(mPort.equalsIgnoreCase(orgP)) {
                String resSplit[] = res.split(",");
                cursor.addRow(new String[]{resSplit[0], resSplit[1]});
                chkVal2 = false;
                return cursor;
            }
            else
            {
                Log.d("Not OrigP","this is not Original port so forwarding to Origport");
                queryInsertSucc(orgP, select, orgP, res);
            }
        }

        while(chkVal2)
        {

        }

//        chkVal2=true;
        return cursor;
    }

    public void queryInsertSucc(String port,String selection,String origPort,String kvp)
    {

        Log.d("in qIS","getting here in QIS:"+port);
        try {
            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    Integer.parseInt(port));

//            String msgSplit[] = info.split(",");
            QueryClass1 tObj = new QueryClass1();

            tObj.setMsg(selection);
            tObj.setPort(port);
            tObj.setOrigPort(origPort);
            tObj.setKvp(kvp);

            ObjectOutputStream spObject = new ObjectOutputStream(socket.getOutputStream());
            Log.d("coming","coming here before write");
            spObject.writeObject(tObj);
            Log.d("sent","successfully sent:"+selection+",to port:"+port);


            spObject.close();
//            socket.close();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void queryInsert(String port,String selection,String origPort,String kvp)
    {
        Log.d("Try", "Send to Pred/Orig:"+port);
        try {
            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    Integer.parseInt(port));

//            String msgSplit[] = info.split(",");
            QueryClass2 tObj = new QueryClass2();

            tObj.setMsg(selection);
            tObj.setPort(port);
            tObj.setOrigPort(origPort);
            tObj.setKvp(kvp);

            ObjectOutputStream spObject = new ObjectOutputStream(socket.getOutputStream());
            spObject.writeObject(tObj);


            spObject.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void addToCursor(HashMap<String,String> curs,String origPort,String sucPort,String stat)
    {
        try {
            Log.d("fwd","from init to be forwarded to:"+sucPort);
            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    Integer.parseInt(sucPort));

            CursorClass cObj=new CursorClass();
            cObj.setOrigPort(origPort);
            cObj.setCurs(curs);
            cObj.setThisPort(sucPort);
            cObj.setStat(stat);
            ObjectOutputStream msgObject = new ObjectOutputStream(socket.getOutputStream());

            msgObject.writeObject(cObj);
            msgObject.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public void addSingleRec(String curs,String origPort,String sucPort,String selection)
    {
        try {
            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    Integer.parseInt(sucPort));

            SingleRecord cObj=new SingleRecord();
            cObj.setOrigPort(origPort);
            cObj.setCurs(curs);
            cObj.setThisPort(sucPort);
            cObj.setSelection(selection);
            ObjectOutputStream msgObject = new ObjectOutputStream(socket.getOutputStream());
            Log.d("coming:","in addSingleRec");
            msgObject.writeObject(cObj);
            msgObject.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MatrixCursor init(HashMap<String,String> stMp,String origPort,String sucPort,String stat) throws InterruptedException {

//        Boolean var=true;

        Log.d("stat:","stat:"+stat+",mPort:"+mPort+",origPort:"+origPort);

        Log.d("in init","coming in init funct after * with mPort:"+mPort+" and origPort:"+origPort+" cursor size:"+cursor.getCount());
        if(sucPort.equalsIgnoreCase("null") || sucPort.equalsIgnoreCase("0"))
        {
            for (Map.Entry<String, String> ent : stMp.entrySet()) {
                cursor.addRow(new String[]{ent.getKey(), ent.getValue()});
            }
        }
//        MatrixCursor curs1=curs;
//        if(sucPort.equalsIgnoreCase("null") || sucPort==null)
//            var=false;
        else if(mPort.equalsIgnoreCase(origPort)) {

            Log.d("mp==op","in init mPort=OrigPort"+" and stMap size:"+stMp.size()+" and stat:"+stat);
            if(stat.equalsIgnoreCase("me"))
            {
                Log.d("forward","forward from init to succ port:"+sucPort);

                addToCursor(stMp,origPort,sucPort,stat);

            }
            else if(stat.equalsIgnoreCase("notme"))
            {
                for (Map.Entry<String, String> ent : stMp.entrySet()) {
                    cursor.addRow(new String[]{ent.getKey(), ent.getValue()});
                }
            }

            var = false;
        }
        else
        {
            Log.d("forward","forward from init to succ port:"+sucPort);
            addToCursor(stMp,origPort,sucPort,stat);
        }

//        while(var)
//        {
//
//        }
        Thread.sleep(1200);

//        return curs1;

        return cursor;
    }

    private class ClientTask1 extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {

                Log.d("coming in CT1","coming in client task");
                String curs=msgs[0];
                String origPort=msgs[1];
                String sucPort=msgs[2];
                String selection=msgs[3];

                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(sucPort));

                SingleRecord cObj=new SingleRecord();
                cObj.setOrigPort(origPort);
                cObj.setCurs(curs);
                cObj.setThisPort(sucPort);
                cObj.setSelection(selection);
                ObjectOutputStream msgObject = new ObjectOutputStream(socket.getOutputStream());
                msgObject.writeObject(cObj);
                msgObject.close();
                socket.close();

                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
//                socket.close();

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }


    public MatrixCursor initSingle(String curs,String origPort,String sucPort,String selection) throws InterruptedException {
//        MatrixCursor cursor=new MatrixCursor(new String[]{"key", "value"});

//        var=true;
//        if(sucPort.equalsIgnoreCase("null") || sucPort==null)
//            var=false;

//        if(mPort.equalsIgnoreCase(origPort))
//            var=false;
        Log.d("key value pair:","key value pair being passed:"+curs+",succ port:"+sucPort);
        if(curs.contains(",") && mPort.equalsIgnoreCase(origPort))
        {
            Log.d("cond satisfied","contains comma and mport now original and var is:"+var);
            cursSplit=curs.split(",");
//            cursor = new MatrixCursor(new String[]{"key","value"});
            cursor.addRow(new String[]{cursSplit[0],cursSplit[1]});
//            var=false;
//            Log.d("curs","cursor size bfore return:"+cursor.getCount());

            return cursor;

        }
        else
        {
            Log.d("else?","coming in else");
//            var=true;

            new ClientTask1().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,curs, origPort,sucPort,selection);

        }

        Thread.sleep(1200);


        return cursor;
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // TODO Auto-generated method stub
        Log.d("query","check query");
        Log.d("cond","cond in query"+cond);
        fileDir=new File(getContext().getApplicationContext().getFilesDir().toString());
        dirFiles = fileDir.listFiles();

        try {


            if (selection.equalsIgnoreCase("\"*\"")) {

                MatrixCursor curs = new MatrixCursor(new String[]{"key", "value"});
                cursor = new MatrixCursor(new String[]{"key","value"});

                Log.d("dirFiles","length of dirFiles:"+dirFiles.length);
//                String fileDir=getContext().getApplicationContext().getFilesDir().toString();
                if (dirFiles.length != 0)
                {
                    // loops through the array of files, outputing the name to console

                    for (int ii = 0; ii < dirFiles.length; ii++)
                    {
                        String fileOutput = dirFiles[ii].toString();
                        fileSplit = fileOutput.split("/");
                        try
                        {
                            fis = getContext().openFileInput(fileSplit[fileSplit.length - 1]);
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader buffreader = new BufferedReader(isr);
                            readString = buffreader.readLine();
                            if(!starQuery.containsKey(fileSplit[fileSplit.length - 1]))
                                starQuery.put(fileSplit[fileSplit.length - 1],readString);
//                            cursor.addRow(new String[]{fileSplit[fileSplit.length - 1], readString});

//                            starQuery.put(fileSplit[fileSplit.length - 1],readString);

//                        System.out.println(fileOutput);
                            Log.d("file:", fileSplit[fileSplit.length - 1]);
                            isr.close();
                            fis.close();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    Log.d("in *","coming in * query");
//                    Log.d("cursor size:", "Cursor size:" + curs.getCount());
                    Log.d("My succ in Query:", "" + nBorMap.get(mPort).getSuc());



                }


                return init(starQuery,mPort,nBorMap.get(mPort).getSuc(),"me");
//                curs = init(curs,mPort,nBorMap.get(mPort).getSuc());
//
//
//                Log.v("directory in *", ""+getContext().getApplicationContext().getFilesDir());
//                return curs;
            }
            else if (selection.equalsIgnoreCase("\"@\""))
            {

//                String fileDir=getContext().getApplicationContext().getFilesDir().toString();
                if (dirFiles.length != 0) {
                    // loops through the array of files, outputing the name to console
                    MatrixCursor curs = new MatrixCursor(new String[]{"key", "value"});
                    for (int ii = 0; ii < dirFiles.length; ii++) {
                        String fileOutput = dirFiles[ii].toString();

                        fileSplit=fileOutput.split("/");
                        fis = getContext().openFileInput(fileSplit[fileSplit.length-1]);
                        InputStreamReader isr = new InputStreamReader(fis);
                        BufferedReader buffreader = new BufferedReader(isr);
                        readString = buffreader.readLine();
                        curs.addRow(new String[]{fileSplit[fileSplit.length-1],readString});

//                        System.out.println(fileOutput);
                        Log.d("file:",fileSplit[fileSplit.length-1]);
                        isr.close();
                        fis.close();
                    }

                    Log.d("cursor size:",""+curs.getCount());
                    return curs;
                }

                Log.v("directory in *", ""+getContext().getApplicationContext().getFilesDir());
            }

            else
            {


                Log.d("new msg","New Single Message:"+selection);
                cursor=new MatrixCursor(new String[]{"key", "value"});


                //Working Code for single Query

                //**************************************STARTS HERE**************************************************
                Log.d("new Single","New Single message");
                flg=0;
//                File fileDir=new File(getContext().getApplicationContext().getFilesDir().toString());
//                File[] dirFiles = fileDir.listFiles();
//                String fileSplit[];
                String fName="";

                String keyValPair="";

                if (dirFiles.length != 0) {
                    for (int ii = 0; ii < dirFiles.length; ii++) {
                        String fileOutput = dirFiles[ii].toString();
                        fileSplit = fileOutput.split("/");
                        if(selection.equalsIgnoreCase(fileSplit[fileSplit.length-1]))
                        {
                            flg=1;
                            fName=fileSplit[fileSplit.length-1];
                            break;
                        }

                    }
                }


                Log.d("flag val","flag is:"+flg);
                if(flg==1) {
                    Log.v("selection is:", "" + selection);
                    fis = getContext().openFileInput(fName);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader buffreader = new BufferedReader(isr);
                    readString = buffreader.readLine();

                    keyValPair=keyValPair+selection;
                    keyValPair=keyValPair+","+readString;

                    Log.v("query res:", "" + readString);
                    cols1[0] = selection;
                    cols1[1] = readString;

                    starMap.put(selection, readString);
                    isr.close();
                    fis.close();
                }




                return initSingle(keyValPair,mPort,nBorMap.get(mPort).getSuc(),selection);
                //*************************************ENDS HERE*******************************************************

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // TODO Auto-generated method stub
        return null;
    }



    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}