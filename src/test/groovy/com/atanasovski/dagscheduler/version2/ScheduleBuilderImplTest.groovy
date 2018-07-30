package com.atanasovski.dagscheduler.version2

import spock.lang.Specification
import spock.lang.Unroll

import static com.atanasovski.dagscheduler.version2.TaskBuilder.task

class ScheduleBuilderImplTest extends Specification {
    def "duplicate taskIds are not allowed"() {
        def taskId = "task1"
        given:
        def task1 = task(taskId).of(Task.class)
        def task2 = task(taskId).of(Task.class)
        when:
        new ScheduleBuilderImpl(task1, task2)

        then:
        thrown(IllegalArgumentException)
    }

    def "when adding a single dependency with 'where' taskId must be present"() {
        given:
        def someTask = task("someTask").of(Task.class)
        def wrongTaskId = "someOtherTask"
        def scheduleBuilder = new ScheduleBuilderImpl(someTask)

        when:
        scheduleBuilder.where(wrongTaskId)

        then:
        thrown(IllegalArgumentException)
    }

    def "state of templates array modified outside of schedule builder does not affect builder"() {
        given:
        def someTaskId = "someTask"
        def someOtherTaskId = "secondTask"
        def someTasks = [task(someTaskId).of(Task.class)] as TaskTemplate[]
        def scheduleBuilder = new ScheduleBuilderImpl(someTasks)
        def newTask = task(someOtherTaskId).of(Task.class)

        when:
        someTasks[0] = newTask

        then:
        scheduleBuilder.hasTask(someTaskId)
        !scheduleBuilder.hasTask(someOtherTaskId)
    }

    def "state of templa1tes array modified outside of schedule builder does not affect builder"() {
        given:
        def someTaskId = "someTask"
        def someOtherTaskId = "secondTask"
        def thirdTaskid = "thirdTask"
        def someTasks = [task(someTaskId).of(Task.class),
                         task(someOtherTaskId).of(Task.class),
                         task(thirdTaskid).of(Task.class)] as TaskTemplate[]
        def dependenciesToAdd = new HashMap<String, String>()
        dependenciesToAdd.put(someTaskId, someOtherTaskId)

        def scheduleBuilder = new ScheduleBuilderImpl(someTasks)
        scheduleBuilder.addDependencies(dependenciesToAdd)

        when:
        dependenciesToAdd.clear()
        def hasDep1to2 = scheduleBuilder.hasDependency(someTaskId, someOtherTaskId)
        dependenciesToAdd.put(someOtherTaskId, thirdTaskid)
        def hasDep2to3 = scheduleBuilder.hasDependency(someOtherTaskId, thirdTaskid)


        then:
        hasDep1to2
        !hasDep2to3
    }

    def "has task tells if the builder has a task with specified id"() {
        given:
        def someTaskId = "someTask"
        def wrongTaskId = "someOtherTask"
        def someTask = task(someTaskId).of(Task.class)
        def scheduleBuilder = new ScheduleBuilderImpl(someTask)

        when:
        def taskIsThere = scheduleBuilder.hasTask(someTaskId)
        def taskIsNotThere = scheduleBuilder.hasTask(wrongTaskId)

        then:
        taskIsThere
        !taskIsNotThere
    }

    @Unroll
    def "has dependency from-to"() {
        given:
        def tasks = taskIds.stream()
                .map { id -> new TaskTemplate(id, Task.class) }
                .toArray() as TaskTemplate[]
        def scheduleBuilder = new ScheduleBuilderImpl(tasks)
        scheduleBuilder.addDependencies(dependecies)

        when:
        def hasDep = scheduleBuilder.hasDependency(taskIds[0], taskIds[1])
        then:
        hasDep == expectedHasDep

        where:
        taskIds                        | dependecies                              | expectedHasDep
        ["task 1", "task 2"]           | ["task 1": "task 2"]                     | true
        ["task 1", "task 2"]           | ["task 2": "task 1"]                     | false
        ["task 1", "task 2", "task 3"] | ["task 1": "task 3", "task 2": "task 3"] | false
        ["task 1", "task 3", "task 2"] | ["task 2": "task 1", "task 1": "task 3"] | true
    }
}
