package edu.buffalo.cse.cse486586.groupmessenger2;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by kamakshishete on 28/02/16.
 */
public class Message implements java.io.Serializable {

    public String msg;
    public int priority;
    public int msgType;
    public int SeqNoFIFO;
    public boolean flag;
    public int sender;
    public int agreedproposal=-1;
    public Message(String Msg,int no,int typeno,int seqnofifo,int sentby){
        msg=Msg;
        priority=no;
        msgType=typeno; //0 FOR STEP 1, 1 FOR STEP 2, 2 FOR STEP 3
        SeqNoFIFO=seqnofifo;
        sender=sentby;

    }
    public Message(String Msg,int no,int typeno,int seqnofifo,int sentby,int agreedpid){
        msg=Msg;
        priority=no;
        msgType=typeno; //0 FOR STEP 1, 1 FOR STEP 2, 2 FOR STEP 3
        SeqNoFIFO=seqnofifo;
        sender=sentby;
        agreedproposal=agreedpid;

    }
    public Message(String Msg,int typeno){
        msg=Msg;
        //priority=no;
        msgType=typeno; //0 FOR STEP 1, 1 FOR STEP 2, 2 FOR STEP 3
        //SeqNoFIFO=seqnofifo;
        //sender=sentby;
        //agreedproposal=agreedpid;

    }


    /*public int compare(Message message1, Message message2) {
        if (message1.priority!=message2.priority)
        {
            return message1.priority - message2.priority;
        }
        else
        {
            if(message1.flag)==true
            return Integer.valueOf(message1.sender) - Integer.valueOf(message2.sender);

        }

    }*/
   /* public static class MsgComparator implements Comparator<Message> {
        @Override
        public int compare(Message msg1, Message msg2) {
            if(msg1==null){
                return 1;
            } else if(msg2==null){
                return -1;
            }
            int result = Integer.compare(msg1.priority, msg2.priority);
            if (result == 0) {
                result = Boolean.compare(msg1.flag, msg2.flag);
                if (result == 0) {
                    result = Integer.compare(Integer.valueOf(msg1.sender), Integer.valueOf(msg2.sender));
                    return result;
                } else {
                    return result;
                }
            } else {
                return result;
            }
        }
    }*/
    @Override
     public String toString(){
        String msg="message:" + this.msg +" "+ "priority:" + this.priority+" " + "msgType:" + this.msgType+ " "+ "SeqNoFIFO" + this.SeqNoFIFO+" " +
                "flag:" + this.flag+" "+ "sender:" + this.sender+ " "+ "agreedproposal"+this.agreedproposal+" ";
        return msg;




    }


}
