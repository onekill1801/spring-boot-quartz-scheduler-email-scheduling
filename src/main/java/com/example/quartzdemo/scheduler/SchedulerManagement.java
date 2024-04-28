package com.example.quartzdemo.scheduler;

import org.quartz.*;
import org.quartz.impl.calendar.AnnualCalendar;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.GregorianCalendar;

@Service
public class SchedulerManagement {

    private final Logger log = LoggerFactory.getLogger(SchedulerManagement.class);

    @Autowired
    private Scheduler scheduler;

    public void pauseTriggers(String group) {
        try {
            scheduler.pauseTriggers(GroupMatcher.triggerGroupEquals(group));
        } catch (SchedulerException e) {
            log.error("pauseTriggers failed: {}", group);
        }
    }

    public void pauseTrigger(String nameId, String groupId) {
        try {
            scheduler.pauseTrigger(new TriggerKey(nameId, groupId));
        } catch (SchedulerException e) {
            log.error("pauseTrigger failed: {}", nameId);
        }
    }

    public void resumeTrigger(String nameId, String groupId) {
        try {
            scheduler.resumeTrigger(new TriggerKey(nameId, groupId));
        } catch (SchedulerException e) {
            log.error("resumeTrigger failed: {}", nameId);
        }
    }

    public void resumeTriggers(String group) {
        try {
            scheduler.resumeTriggers(GroupMatcher.triggerGroupEquals(group));
        } catch (SchedulerException e) {
            log.error("resumeTriggers failed: {}", group);
        }
    }

    public void scheduleJobWithCalendar(JobDetail jobDetail, Trigger trigger) {
        AnnualCalendar calendar = new AnnualCalendar();
        Calendar excludedDate = new GregorianCalendar();
        excludedDate.set(Calendar.MONTH, Calendar.DECEMBER);
        excludedDate.set(Calendar.DAY_OF_MONTH, 25); // Christmas day
        calendar.setDayExcluded(excludedDate, true); // Exclude Christmas day
        try {
            scheduler.addCalendar(jobDetail.getKey().getName(), calendar, true, true);
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
        } catch (SchedulerException e) {
            log.error("scheduleJobWithCalendar failed: {}", jobDetail.getKey().toString());
        }
    }

    public void scheduleJob(JobDetail jobDetail, Trigger trigger) {
        try {
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
        } catch (SchedulerException e) {
            log.error("scheduleJob failed: {}", jobDetail.getKey().toString());
            e.printStackTrace();
        }
    }

}
