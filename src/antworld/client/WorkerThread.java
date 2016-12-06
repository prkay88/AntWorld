package antworld.client;

import antworld.common.AntAction;
import antworld.common.AntData;
import antworld.common.CommData;

import java.util.ArrayList;

/**
 * Created by Phillip on 12/1/2016.
 */
public class WorkerThread extends Thread
{
    private ArrayList<AntData> antDataList = new ArrayList<>();
    private AI intelligence;
    private CommData commData;

    public WorkerThread(ArrayList<AntData> antDataList, CommData commData)
    {
        this.antDataList = antDataList;
        this.commData = commData;
    }

    public void setAntDataList(ArrayList<AntData> antDataList)
    {
        this.antDataList =antDataList;
    }

    public void setIntelligence(AI intelligence)
    {
        this.intelligence = intelligence;
    }

    public AI getIntelligence()
    {
        return this.intelligence;
    }

    public void setCommData(CommData commData)
    {
        this.commData = commData;
    }

    @Override
    public void run()
    {
        intelligence.setCommData(commData);

        for(AntData antData : commData.myAntList)
        {

            intelligence.setAntData(antData);
           antData.myAction = intelligence.chooseAction();

           //System.out.println("Ant ID: " +antData.id + " chose " + antData.myAction);

        }
    }
}
