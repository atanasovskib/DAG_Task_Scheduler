package com.atanasovski.dagscheduler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Blagoj on 24-Feb-16.
 */
public abstract class Executable implements Runnable {
    private final String id;
    private Map<String, List<Object>> inputParameters = new HashMap<>();
    private Map<String, List<Object>> outputParameters = new HashMap<>();
    private Scheduler scheduler;
    private List<String> errors = new LinkedList<>();

    public Executable(Scheduler scheduler, String id) {
        this.scheduler = scheduler;
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

    protected List<Object> get(String inputName) {
        return inputParameters.get(inputName);
    }

    protected final <T> void produce(String outputName, T result) {
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
        System.out.println("Starting exe: " + this.getId());
        this.errors.clear();
        this.execute();
        System.out.println("Done exe: " + this.getId());
        if (this.errors.isEmpty()) {
            this.scheduler.notifyDone(this);
        } else {
            this.scheduler.notifyError(this.errors);
        }
    }

    public void addInputParameters(Map<String, List<Object>> parameters) {
        parameters.entrySet().forEach(entry -> {
            if (!this.inputParameters.containsKey(entry.getKey())) {
                this.inputParameters.put(entry.getKey(), new LinkedList<>());
            }

            this.inputParameters.get(entry.getKey()).addAll(entry.getValue());
        });
    }

    public final List<String> getErrors() {
        return this.errors;
    }
}
