package com.atanasovski.dagscheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class Executable implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(Executable.class);
    private final String id;
    private Map<String, List<Object>> inputParameters = new HashMap<>();
    private Map<String, List<Object>> outputParameters = new HashMap<>();
    private Scheduler scheduler;
    private List<String> errors = new LinkedList<>();
    private float executionWeight = -1;
    private int executionTime = 0;

    public void setExecutionWeight(float weight) {
        this.executionWeight = weight;
    }

    protected Executable(String id, int executionTimeEstimate) {
        this.id = id;
        this.executionTime = executionTimeEstimate;
    }

    protected Executable(String id) {
        this.id = id;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Executable that = (Executable) o;

        return id.equals(that.id);
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }

    public abstract void execute();

    protected List<? extends Object> get(String inputName) {
        return inputParameters.get(inputName);
    }

    protected final void produce(String outputName, Object result) {
        if (!outputParameters.containsKey(outputName)) {
            this.outputParameters.put(outputName, new LinkedList<>());
        }

        this.outputParameters.get(outputName).add(result);
    }

    protected final void error(String error) {
        this.errors.add(error);
    }

    public Map<String, List<Object>> getOutputParameters() {
        return outputParameters;
    }

    private final void setOutputParameters() {
    }

    private final Map<String, List<Object>> getInputParameters() {
        return null;
    }

    public final String getId() {
        return id;
    }

    @Override
    public void run() {
        Objects.requireNonNull(this.scheduler, "Scheduler not set, can not execute");

        logger.info("Starting exe: {}", this.getId());
        this.errors.clear();
        this.execute();
        logger.info("Done exe: {}", this.getId());
        if (this.errors.isEmpty()) {
            this.scheduler.notifyDone(this);
        } else {
            this.scheduler.notifyError(this.errors);
        }
    }

    public Executable addInput(Map<String, List<Object>> parameters) {
        parameters.forEach((key, value) -> {
            if (!this.inputParameters.containsKey(key)) {
                this.inputParameters.put(key, new LinkedList<>());
            }

            this.inputParameters.get(key).addAll(value);
        });

        return this;
    }

    public Executable addInput(String parameterName, Object... values) {
        if (!this.inputParameters.containsKey(parameterName)) {
            this.inputParameters.put(parameterName, new LinkedList<>());
        }

        this.inputParameters.get(parameterName).addAll(Arrays.asList(values));
        return this;
    }

    public Executable addInput(String parameterName, Collection<?> values) {
        if (!this.inputParameters.containsKey(parameterName)) {
            this.inputParameters.put(parameterName, new LinkedList<>());
        }

        this.inputParameters.get(parameterName).addAll(values);
        return this;
    }

    public final List<String> getErrors() {
        return this.errors;
    }

    public void setScheduler(Scheduler s) {
        this.scheduler = s;
    }

    public int getExecutionTime() {
        return executionTime;
    }

    public float getExecutionWeight() {
        return executionWeight;
    }

    public boolean hasExecutionWeight() {
        return this.executionWeight != -1;
    }

    @Override
    public String toString() {
        return this.getId();
    }
}
