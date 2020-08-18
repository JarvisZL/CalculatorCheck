package com.example.projectone;

public class Result {

    private final int mNumber;
    private final float mProbability;
    private final long mTimeCost;

    public Result(float[] probs, long timeCost) {
        mNumber = argmax(probs);
        mProbability = probs[mNumber];
        mTimeCost = timeCost;
    }

    /* 编码方式
        0-9： 0-9
        +： 10
        -： 11
        *： 12
        /： 13
        =： 14
     */
    public String getchar(){
        if(mNumber < 10)
            return String.valueOf(mNumber);
        else if(mNumber == 10)
            return String.valueOf('+');
        else if(mNumber == 11)
            return String.valueOf('-');
        else if(mNumber == 12)
            return String.valueOf('*');
        else if(mNumber == 13)
            return String.valueOf('/');
        else if(mNumber == 14)
            return String.valueOf('=');
        else
            return null;
    }
    public int getmNumber() { return mNumber; }
    public float getProbability() {
        return mProbability;
    }
    public long getTimeCost() {
        return mTimeCost;
    }

    private static int argmax(float[] probs) {
        int maxIdx = -1;
        float maxProb = 0.0f;
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > maxProb) {
                maxProb = probs[i];
                maxIdx = i;
            }
        }
        return maxIdx;
    }
}