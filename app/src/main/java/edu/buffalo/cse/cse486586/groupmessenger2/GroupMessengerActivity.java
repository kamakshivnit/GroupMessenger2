package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.content.ContentValues;
import android.net.Uri;
import android.view.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import android.view.View;
import android.widget.EditText;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import android.widget.Button;
import java.util.Collections;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 */
public class GroupMessengerActivity extends Activity {
    private static final String TAG = GroupMessengerActivity.class.getSimpleName();
    private static final int SERVER_PORT = 10000;
    private static final int[] REMOTE_PORTS = {11108, 11112, 11116, 11120, 11124};
    private static int sequenceNo = 0;
    private static int sequenceNo1= 0;
    private static int SequenceNoFIFO = 0;
    private static int NODES = 5;
    private static int ProposedsequenceNo = 0;
    private static boolean[] IsAlive={true,true,true,true,true};
    private static boolean[] DataCleaned={false,false,false,false,false};
    private static HashMap<Integer,Integer> SeqMap=new HashMap<Integer, Integer>();
    private static HashMap<Integer,PriorityQueue<Message>> MessageBufferMap=new HashMap<Integer,PriorityQueue<Message>>();
    private Uri providerUri;
    private static Message messageinthefront= new Message("initial",1,2,0,11108);
    private static Message oldmessageinthefront= new Message("oldinitial",1,2,1,11108);

    private static PriorityQueue<Message> MessageQueue = new PriorityQueue<Message>(10, new Comparator<Message>() {
        // Overriding the compare method to sort the age
        public int compare(Message message1, Message message2) {
            if (message1.priority!=message2.priority)
            {
                return message1.priority - message2.priority;
            }
            else
            {


                if(message1.flag==message2.flag)
                {
                    return message1.agreedproposal - message2.agreedproposal;
                }
                else
                {
                    if((message1.flag==true)&&(message2.flag==false))
                    {
                        return 1;
                    }
                    return -1;
                }

            }

        }

    });
    //private static PriorityQueue<Message> MessageQueue = new PriorityQueue<Message>(10, new Message.MsgComparator());
    private static int[] Rg={-1,-1,-1,-1,-1};
    private static ArrayList<Message> HoldBackQueue=new ArrayList<Message>();
    private static int myPort=0;
    //private HashMap<Integer,ArrayList<Integer>> proposedprioritymap=new HashMap<Integer,ArrayList<Integer>>();
    private HashMap<Integer,ArrayList<Message>> proposedprioritymap=new HashMap<Integer,ArrayList<Message>>();



    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);
/*
         * Calculate the port number that this AVD listens on.
         * It is just a hack that I came up with to get around the networking limitations of AVDs.
         * The explanation is provided in the PA1 spec.
         */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //nullpointer exception
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = (Integer.parseInt(portStr) * 2);

        messageinthefront.flag=true;
        oldmessageinthefront.flag=true;
        Initialize();

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket" + e);
            //return;
        }
        /* try
        {
            new PingTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,portStr);
        }
        catch (Exception e) {
            Log.e(TAG, "Can't create a pingservice" + e);
            //return;
        }*/
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        final EditText editText = (EditText) findViewById(R.id.editText1);
        Button send = (Button) findViewById(R.id.button4);
        //findViewById(R.id.button4).setOnClickListener(
        send.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String msg = editText.getText().toString();
                        //String msg = editText.getText().toString() + "\n";
                        editText.setText(""); // This is one way to reset the input box.
                        //tv.append("\t" + msg); // This is one way to display a string.
                        String logmsg="onclick"+msg;
                        Log.v(TAG, logmsg);
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg,Integer.toString(myPort));


                    }
                });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GroupMessenger Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://edu.buffalo.cse.cse486586.groupmessenger2/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "GroupMessenger Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://edu.buffalo.cse.cse486586.groupmessenger2/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            //Reference:https://docs.oracle.com/javase/tutorial/networking/sockets/
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    ObjectInputStream Obin = new ObjectInputStream(socket.getInputStream());
                    Message incoming = (Message) Obin.readObject();

                    int index=(incoming.sender-11108)/4;
                    String logmsg="Message Received:" + incoming.toString();
                    Log.v(TAG,logmsg);

                    if (incoming.msgType == 0)
                    {
                        logmsg="Message "+incoming.toString()+" received at this avd is assigned priority: "+ ProposedsequenceNo;
                        Log.v(TAG,logmsg);
                        incoming.priority = ProposedsequenceNo;
                        incoming.flag = false;
                        //Storing message in a message queue:

                        MessageQueue.offer(incoming);
                        messageinthefront=MessageQueue.peek();
                        Message msg1=new Message(incoming.msg,ProposedsequenceNo,1,incoming.SeqNoFIFO,myPort);
                        try {
                            Socket socketsend = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    incoming.sender);

                            ObjectOutputStream Obout = new ObjectOutputStream(socketsend.getOutputStream());

                            Obout.writeObject(msg1);
                            Obout.close();
                            socketsend.close();
                        }catch (Exception e)
                        {
                            logmsg="Failure at Node at proposal sending step "+ incoming.sender+" "+e.toString();
                            Log.e(TAG, logmsg);
                            Message m=new Message(Integer.toString(incoming.sender),3);
                            Multicast(m);
                        }
                        ProposedsequenceNo++;
                        //pinging everyone to check for heartbeats
                        //Message msg2=new Message("ping",0,4,0,11111);
                        //Multicast(msg2);

                    }


                    //if it is sender avd itself and message is proposed priority
                    if(incoming.msgType==1)
                    {
                        if (proposedprioritymap.containsKey(incoming.SeqNoFIFO))
                        {
                            //proposedprioritymap.get(incoming.SeqNoFIFO).add(incoming.priority);
                            proposedprioritymap.get(incoming.SeqNoFIFO).add(incoming);
                            //check if all are present
                            if (proposedprioritymap.get(incoming.SeqNoFIFO).size()==NODES)
                            {
                                //pick a max value and send it to all AVDs

                                ArrayList<Message> MessageList =proposedprioritymap.get(incoming.SeqNoFIFO);
                                /*logmsg="Priorities present in the list are "+MessageList.get(0).priority+ " "+MessageList.get(1).priority+" "+
                                        MessageList.get(2).priority+" "+MessageList.get(3).priority+" "+MessageList.get(4).priority;*/
                                Log.v(TAG,logmsg);
                                /*logmsg="Nodes proposed present in the list are "+MessageList.get(0).sender+ " "+MessageList.get(1).sender+" "+
                                        MessageList.get(2).sender+" "+MessageList.get(3).sender+" "+MessageList.get(4).sender;*/
                                Log.v(TAG,logmsg);
                                int priority=-99999;
                                int agreedproposal=-1;
                                for(int i=0;i<MessageList.size();i++)
                                {

                                    if(MessageList.get(i).priority > priority)
                                    {
                                        priority=MessageList.get(i).priority;
                                        //agreedproposal=incoming.sender;
                                        agreedproposal=MessageList.get(i).sender;

                                    }
                                    else if(MessageList.get(i).priority == priority) {
                                        agreedproposal=Math.max(agreedproposal,MessageList.get(i).sender);

                                    }


                                }
                                proposedprioritymap.remove(incoming.SeqNoFIFO);
                                //int priority= Collections.max(proposedprioritymap.get(incoming.SeqNoFIFO));
                                Message msg2=new Message(incoming.msg,priority,2,incoming.SeqNoFIFO,myPort,agreedproposal);
                                //reused code
                                logmsg="Message sent with agreed priority "+ msg2.toString();
                                Log.v(TAG,logmsg);

                                //multicasting agreed priority to everyone
                                //write a code for multicasting a message and check for exception
                                //write a code for clean up
                                Multicast(msg2);

                            }


                        }
                        else
                        {
                            ArrayList<Message> proposedpriorities= new ArrayList<Message>();
                            proposedpriorities.add(incoming);
                            proposedprioritymap.put(incoming.SeqNoFIFO,proposedpriorities);


                        }
                        Message msg1=new Message("ping",0,4,0,11111);
                        Multicast(msg1);
                    }

                    if(incoming.msgType==2)
                    {
                        if (incoming.priority > ProposedsequenceNo)
                        {
                            ProposedsequenceNo=incoming.priority+1;
                        }
                        Iterator it=MessageQueue.iterator();
                        while(it.hasNext())
                        {
                            Message m=(Message)it.next();
                            logmsg="Message in the queue is:"+m.toString();
                            Log.v(TAG,logmsg);
                            if ((m.SeqNoFIFO==incoming.SeqNoFIFO)&&(m.sender==incoming.sender))
                            {

                                //Collections.sort(MessageQueue);
                                logmsg="Message matched";
                                Log.v(TAG, logmsg);
                                MessageQueue.remove(m);
                                m.priority=incoming.priority;
                                m.flag=true;
                                m.agreedproposal=incoming.agreedproposal;
                                //incoming.flag=true;
                                MessageQueue.offer(m);
                                messageinthefront=MessageQueue.peek();

                                break;

                            }

                        }
                        Iterator it1=MessageQueue.iterator();
                        while(it1.hasNext())
                        //    for(int i=0;i<MessageQueue.size();i++)
                        {
                            Message m = (Message) it1.next();
                            logmsg = "msgsinqueuewhiledelivering12345" + m.toString();
                            Log.v(TAG,logmsg);
                        }

                        while(!MessageQueue.isEmpty())
                        {

                            Message m=MessageQueue.peek();
                            logmsg="Msg at front of the queue"+m.toString();
                            Log.v(TAG,logmsg);
                            if (m.flag)
                            {
                                logmsg="Message is deliverable "+m.toString()+"sequence no"+ sequenceNo1;
                                Log.v(TAG, logmsg);

                                //Here check FIFO before delivering// check seq no for the msg sender and compare it with
                                //seqNoFIFO of that msg and it should be plus one if it is publish else put it in a queue.
                                //if you are publishing it also check messages in queue if any messages are waiting for delievrt
                                int val=SeqMap.get(m.sender);
                                PriorityQueue<Message> MessageBuffer=MessageBufferMap.get(m.sender);
                                if (val+1==m.SeqNoFIFO)
                                {
                                    logmsg="Message is deliverable FIFO "+m.toString()+"sequence no"+ sequenceNo1;
                                    Log.v(TAG, logmsg);
                                    publishProgress(m.msg);
                                    sequenceNo1++;
                                    //Removing message at the front of the queue
                                    MessageQueue.poll();
                                    //incrementing counter for that avd
                                    val++;
                                    SeqMap.put(m.sender,val);
                                    //check if there are any messages that are waiting for this msg

                                    while (!MessageBuffer.isEmpty())
                                    {
                                        logmsg="Message checking inside FIFO queue now"+m.toString()+"sequence no"+ sequenceNo1;
                                        Log.v(TAG, logmsg);
                                       Message mf=MessageBuffer.peek();
                                       if (mf.SeqNoFIFO==val+1)
                                       {
                                           publishProgress(mf.msg);
                                           sequenceNo1++;
                                           //Removing message at the front of the queue
                                           MessageBuffer.poll();
                                           //incrementing counter for that avd
                                           val++;
                                           SeqMap.put(m.sender,val);
                                           logmsg="Message is deliverable FIFO "+m.toString()+"sequence no"+ sequenceNo1;

                                       }
                                       else
                                       {
                                           break;
                                       }


                                    }
                                }
                                else
                                {
                                    MessageBuffer.offer(m);

                                }
                            }
                            else
                            {
                                logmsg="Message not deliverable"+m.toString();
                                Log.v(TAG, logmsg);


                                //ping to check if it the sender is failed
                                Message msg1=new Message("test",0,4,0,11111);
                                try {
                                    Socket socketsend = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                            m.sender);

                                    ObjectOutputStream Obout = new ObjectOutputStream(socketsend.getOutputStream());

                                    Obout.writeObject(msg1);
                                    Obout.close();
                                    socketsend.close();
                                }
                                catch (Exception e)
                                {
                                    logmsg="Failure at Node at msg 2 type loop checking step "+ m.sender+" "+e.toString();
                                    Log.e(TAG, logmsg);
                                    Message m1=new Message(Integer.toString(m.sender),3);
                                    Multicast(m1);
                                }

                                break;
                            }

                        }
                        //pinging everyone to check heartbeats
                        /*Message msg1=new Message("ping",0,4,0,11111);
                        Multicast(msg1);*/



                    }
                    if(incoming.msgType==3) {
                        //Now we know that node i has failed, so simply clean up if not already
                        //check if there is anything at all to clear up, first check if flag for that avd alive
                        //if it is not alive,no need to proceed further.
                        //if it is alive,decrease tha max size to decide agreed priority
                        //then go through the priority map and if message size is 4,make decisions.
                        //set all flags in priority queue corresponding to that message to true
                        int failindex = -8;
                        for (int i = 0; i < 5; i++) {
                            if (REMOTE_PORTS[i] == Integer.valueOf(incoming.msg)) {
                                failindex = i;
                                break;
                            }


                        }
                        logmsg = "Failure Handling activated for " + incoming.msg;
                        Log.v(TAG, logmsg);
                        if (!DataCleaned[failindex]) {
                            NODES--;
                            IsAlive[failindex] = false;
                            DataCleaned[failindex]=true;
                            Iterator it = MessageQueue.iterator();
                            while (it.hasNext()) {
                                Message m = (Message) it.next();
                                if (m.sender == Integer.valueOf(incoming.msg)) {
                                    if (!m.flag) {
                                        m.flag = true;
                                    }
                                }
                                logmsg = "Finding messages by failed node to set deliverable" + m.toString();
                                Log.v(TAG, logmsg);
                            }
                            while (!MessageQueue.isEmpty()) {

                                Message m = MessageQueue.peek();
                                logmsg = "Msg at front of the queue--failure detection" + m.toString();
                                Log.v(TAG, logmsg);
                                if (m.flag) {
                                    logmsg = "Message is deliverable--failure detection " + m.toString() + "sequence no" + sequenceNo1;
                                    Log.v(TAG, logmsg);
                                    int val=SeqMap.get(m.sender);
                                    PriorityQueue<Message> MessageBuffer=MessageBufferMap.get(m.sender);
                                    if (val+1==m.SeqNoFIFO)
                                    {
                                        logmsg="Message is deliverable FIFO "+m.toString()+"sequence no"+ sequenceNo1;
                                        Log.v(TAG, logmsg);
                                        publishProgress(m.msg);
                                        sequenceNo1++;
                                        //Removing message at the front of the queue
                                        MessageQueue.poll();
                                        //incrementing counter for that avd
                                        val++;
                                        SeqMap.put(m.sender,val);
                                        //check if there are any messages that are waiting for this msg

                                        while (!MessageBuffer.isEmpty())
                                        {
                                            logmsg="Message checking inside FIFO queue now"+m.toString()+"sequence no"+ sequenceNo1;
                                            Log.v(TAG, logmsg);
                                            Message mf=MessageBuffer.peek();
                                            if (mf.SeqNoFIFO==val+1)
                                            {
                                                publishProgress(mf.msg);
                                                sequenceNo1++;
                                                //Removing message at the front of the queue
                                                MessageBuffer.poll();
                                                //incrementing counter for that avd
                                                val++;
                                                SeqMap.put(m.sender,val);
                                                logmsg="Message is deliverable FIFO "+m.toString()+"sequence no"+ sequenceNo1;

                                            }
                                            else
                                            {
                                                break;
                                            }


                                        }
                                    }
                                    else
                                    {
                                        MessageBuffer.offer(m);

                                    }
                                    //publishProgress(m.msg);
                                   // sequenceNo1++;
                                   // //Removing message at the front of the queue
                                    //MessageQueue.poll();
                                } else {
                                    logmsg = "Message not deliverable-- failure detection" + m.toString();
                                    Log.v(TAG, logmsg);
                                    break;
                                }

                            }

                            //checking
                            Iterator<Integer> iter=proposedprioritymap.keySet().iterator();
                            while (iter.hasNext())
                            //for ( int keySeq : proposedprioritymap.keySet())
                            {
                                Integer keySeq = iter.next();
                                logmsg = "checking Lists now in proposedprioritymap now"+ keySeq;
                                Log.v(TAG, logmsg);
                                //ArrayList<Message> mList = proposedprioritymap.get(keySeq);
                                ArrayList<Message> mList = proposedprioritymap.get(keySeq);

                                int i;boolean found=false;
                                for (i = 0; i < mList.size(); i++) {
                                    if (mList.get(i).sender == Integer.valueOf(incoming.msg)) {
                                        logmsg = "Message found for sender,removing it now"+keySeq;
                                        Log.v(TAG, logmsg);
                                        found=true;
                                        mList.remove(i);
                                        break;

                                    }
                                }
                                if (!found) {
                                    logmsg = "Message not found for sender in proposal array"+keySeq ;
                                    Log.v(TAG, logmsg);
                                    if (mList.size() == NODES) {
                                        int priority = -99999;
                                        int agreedproposal = -1;
                                        for (i = 0; i < mList.size(); i++) {

                                            if (mList.get(i).priority > priority) {
                                                priority = mList.get(i).priority;
                                                //agreedproposal=incoming.sender;
                                                agreedproposal = mList.get(i).sender;

                                            } else if (mList.get(i).priority == priority) {
                                                agreedproposal = Math.max(agreedproposal, mList.get(i).sender);

                                            }


                                        }

                                        //int priority= Collections.max(proposedprioritymap.get(incoming.SeqNoFIFO));
                                        Message msg2 = new Message(mList.get(0).msg, priority, 2, keySeq, myPort, agreedproposal);
                                        //proposedprioritymap.remove(keySeq);
                                        iter.remove();
                                        //reused code
                                        logmsg = "Message sent with agreed priority--failure detection " + msg2.toString();
                                        Log.v(TAG, logmsg);

                                        //multicasting agreed priority to everyone
                                        //write a code for multicasting a message and check for exception
                                        //write a code for clean up
                                        Multicast(msg2);
                                    }

                                }


                            }


                        }
                    }
                    //How to check for failures in ISIS??



                } catch (IOException e) {
                    String logmsg="ServerTask IOException"+ e.toString();
                    Log.e(TAG, logmsg);
                } catch (ClassNotFoundException c) {
                    Log.e(TAG, "ServerTask ClassNotFoundException");
                }
            }
            //return null;
        }

        protected void onProgressUpdate(String... strings) {
            /*
             * The following code displays what is received in doInBackground().
             */

            String strReceived = strings[0].trim();
            TextView tv = (TextView) findViewById(R.id.textView1);
            tv.append(strReceived + "\t\n");

            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */
            ContentValues keyValueToInsert = new ContentValues();
            keyValueToInsert.put("key", sequenceNo);
            keyValueToInsert.put("value", strReceived);

            providerUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
            Uri newUri = getContentResolver().insert(
                    providerUri,    // assume we already created a Uri object with our provider URI
                    keyValueToInsert);
            sequenceNo++;
            return;
        }
    }

    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            Message msgToSend = new Message(msgs[0], 0, 0,SequenceNoFIFO,Integer.parseInt(msgs[1]));
            String logmsg="Message sent by sender "+msgs[1]+ " is "+msgs[0];
            Log.v(TAG,logmsg);
            Multicast(msgToSend);
            SequenceNoFIFO++;
            return null;
        }
    }

    /**
     * buildUri() demonstrates how to build a URI for a ContentProvider.
     *
     * @param scheme
     * @param authority
     * @return the URI
     */
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }



    private void Multicast(Message msg) {
        String logmsg = "Multicast by sender" + msg.msg;
        Log.v(TAG, logmsg);
        int i = 0;
        while (i < 5) {
            try {
                if (IsAlive[i]) {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            REMOTE_PORTS[i]);
                    ObjectOutputStream Obout = new ObjectOutputStream(socket.getOutputStream());
                    //TBDforjson:convert msg2 to json string and pass it as a string

                    Obout.writeObject(msg);
                    Obout.close();
                    socket.close();
                }

            } catch (Exception e) {
                logmsg = "Failure at Node " + REMOTE_PORTS[i] + " " + e.toString();
                Log.e(TAG, logmsg);
                IsAlive[i] = false;
                Message m = new Message(Integer.toString(REMOTE_PORTS[i]), 3);
                Multicast(m);

            }
            i++;
        }
    }

    private void Initialize()
    {
        for(int i=0;i<5;i++)
        {
            PriorityQueue<Message> MessageBuffer = new PriorityQueue<Message>(10, new Comparator<Message>() {
                // Overriding the compare method to sort the age
                public int compare(Message message1, Message message2) {

                    return message1.SeqNoFIFO - message2.SeqNoFIFO;
                }

            });
            MessageBufferMap.put(REMOTE_PORTS[i],MessageBuffer);
            SeqMap.put(REMOTE_PORTS[i],-1);


        }
    }

    private class PingTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            String logmsg = "Entering pingservice ";
            Log.v(TAG, logmsg );
            while(true)
            {
                  try {
                      Thread.sleep(3000);
                      Message newmessageattop = messageinthefront;


                      if ((newmessageattop.SeqNoFIFO == oldmessageinthefront.SeqNoFIFO) &&
                              (newmessageattop.sender == oldmessageinthefront.sender) && (newmessageattop.flag = false)) {
                          //ping the sender
                          try {
                              logmsg = "Checking for Failure in ping step " + newmessageattop.sender;
                              Log.v(TAG, logmsg );
                              Socket socketsend = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                      newmessageattop.sender);

                              ObjectOutputStream Obout = new ObjectOutputStream(socketsend.getOutputStream());
                              //String Msg,int no,int typeno,int seqnofifo,int sentby
                              Message msg1 = new Message("ping", 0, 4, 0, 5000);
                              Obout.writeObject(msg1);
                              Obout.close();
                              socketsend.close();
                          } catch (Exception e) {
                              logmsg = "Failure at Node at detected in ping step " + newmessageattop.sender + " " + e.toString();
                              Log.e(TAG, logmsg);
                              Message m = new Message(Integer.toString(newmessageattop.sender), 3);
                              Multicast(m);
                          }

                      }
                      oldmessageinthefront = messageinthefront;
                  }
                  catch (Exception e)
                  {
                      Log.e(TAG, "Can't sleep thread" );

                  }

            }

        }
    }

}