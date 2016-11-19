package antworld.client;

import antworld.common.AntAction;
import antworld.common.AntData;
import antworld.common.CommData;
import antworld.common.Direction;

import java.util.Random;

/**
 * Created by Phillip on 11/18/2016.
 * This will be the super class that houses all the decision making methods.
 * This class will be extended by particular AI behavoirs.
 */
public class AI {
    CommData commData;
    Random random = new Random();
    int centerX;
    int centerY;
    AntAction ant;
    AntData antData;

    public AI() {

    }

    public AI(CommData commData, AntData antData) {
        ant = new AntAction(AntAction.AntActionType.STASIS);

        this.commData = commData;
        if (this.commData == null) System.out.println("commData is null");
        if (this.commData.myNest == null) System.out.println("myNest is null");
        if (this.commData.nestData == null) System.out.println("nestData is null");
        ant.type = AntAction.AntActionType.STASIS;
        this.antData = antData;
    }

    public AntAction getAntAction() {
        return this.ant;
    }

    public void setAntAction(AntAction ant) {
        this.ant = ant;
    }

    public AntData getAntData() {
        return this.antData;
    }

    public void setAntData(AntData antData) {
        this.antData = antData;
    }

    public CommData getCommData() {
        return this.commData;
    }

    public void setCommData(CommData commData) {
        this.commData = commData;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public void setCenterY(int centerY)
    {
        this.centerY = centerY;
    }



    //=============================================================================
    // This method sets the given action to EXIT_NEST if and only if the given
    //   ant is underground.
    // Returns true if an action was set. Otherwise returns false
    //=============================================================================
    public boolean exitNest()
    {

        return false;
    }


    public boolean attackAdjacent()
    {
        return false;
    }

    public boolean pickUpFoodAdjacent()
    {
        return false;
    }

    public boolean goHomeIfCarryingOrHurt()
    {
        return false;
    }

    public boolean pickUpWater()
    {
        return false;
    }

    public boolean goToEnemyAnt()
    {
        return false;
    }

    public boolean goToFood()
    {
        return false;
    }

    public boolean goToGoodAnt()
    {
        return false;
    }

    public boolean goExplore()
    {

        return false;
    }


    public AntAction chooseAction()
    {
        //AntAction action = new AntAction(AntAction.AntActionType.STASIS);

        if (antData.ticksUntilNextAction > 0) return this.ant;

        if (exitNest()) return this.ant; //always exit nest first

        if (attackAdjacent()) return this.ant;

        if (pickUpFoodAdjacent()) return this.ant;

        if (goHomeIfCarryingOrHurt()) return this.ant;

        if (pickUpWater()) return this.ant;

        if (goToEnemyAnt()) return this.ant;

        if (goToFood()) return this.ant;

        if (goToGoodAnt()) return this.ant;

        if (goExplore()) return this.ant;

        return this.ant;
    }

}
