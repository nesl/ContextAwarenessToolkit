package edu.ucla.nesl.toolkit.executor.common;

import java.util.List;

import edu.ucla.nesl.toolkit.executor.common.module.InferencePipeline;

/**
 * Created by cgshen on 11/13/16.
 */

public class InferenceExecutor {
    private InferencePipeline mInferencePipeline;
    private long interval;
    private long duration;
    private List<List<Float>> dataBuffer;

    public InferenceExecutor() {

    }

    public InferenceExecutor(InferencePipeline mInferencePipeline, long interval, long duration) {
        this.mInferencePipeline = mInferencePipeline;
        this.interval = interval;
        this.duration = duration;
    }

    public InferencePipeline getmInferencePipeline() {
        return mInferencePipeline;
    }

    public void setmInferencePipeline(InferencePipeline mInferencePipeline) {
        this.mInferencePipeline = mInferencePipeline;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
