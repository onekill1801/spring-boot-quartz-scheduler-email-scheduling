package com.example.quartzdemo.trigger;

import org.quartz.Calendar;
import org.quartz.ScheduleBuilder;
import org.quartz.impl.triggers.AbstractTrigger;

import java.util.Date;

public class CustomTrigger extends AbstractTrigger<CustomTrigger> {
    private String customProperty;
    private Date nextFireTime;

    @Override
    public void triggered(Calendar calendar) {

    }

    @Override
    public Date computeFirstFireTime(Calendar calendar) {
        return null;
    }

    @Override
    public boolean mayFireAgain() {
        return false;
    }

    @Override
    public Date getStartTime() {
        return null;
    }

    @Override
    public void setStartTime(Date date) {

    }

    @Override
    public void setEndTime(Date date) {

    }

    @Override
    public Date getEndTime() {
        return null;
    }

    @Override
    public Date getNextFireTime() {
        return null;
    }

    @Override
    public Date getPreviousFireTime() {
        return null;
    }

    @Override
    public Date getFireTimeAfter(Date date) {
        return null;
    }

    @Override
    public Date getFinalFireTime() {
        return null;
    }

    @Override
    protected boolean validateMisfireInstruction(int i) {
        return false;
    }

    @Override
    public void updateAfterMisfire(Calendar calendar) {

    }

    @Override
    public void updateWithNewCalendar(Calendar calendar, long l) {

    }

    @Override
    public void setNextFireTime(Date date) {

    }

    @Override
    public void setPreviousFireTime(Date date) {

    }

    @Override
    public ScheduleBuilder<CustomTrigger> getScheduleBuilder() {
        return null;
    }

    public void setCustomProperty(String customProperty) {
        this.customProperty = customProperty;
    }

    public String getCustomProperty() {
        return customProperty;
    }
}
