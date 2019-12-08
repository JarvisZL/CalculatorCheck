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

    public String getNumber() {
        if(mNumber < 10)
            return String.valueOf(mNumber);
        else if(mNumber < 36){
            return String.valueOf((char)(mNumber-10+65));
        }
        else
            return String.valueOf((char)(mNumber-36+97));
    }

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