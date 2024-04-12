package com.example.quartzdemo.controller;

import com.example.quartzdemo.job.EmailJob;
import com.example.quartzdemo.payload.ScheduleEmailRequest;
import com.example.quartzdemo.payload.ScheduleEmailResponse;
import com.example.quartzdemo.trigger.CustomTrigger;
import org.quartz.*;
import org.quartz.impl.calendar.AnnualCalendar;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.*;

@RestController
public class EmailJobSchedulerController {
    private static final Logger logger = LoggerFactory.getLogger(EmailJobSchedulerController.class);

    @Autowired
    private Scheduler scheduler;

    public static <T> T convertJobDataMapObject(JobDataMap jobDataMap, Class<T> clazz) throws IllegalAccessException, InstantiationException {
//        ScheduleEmailRequest object = new ScheduleEmailRequest();
        T object = clazz.newInstance();
        BeanWrapper beanWrapper = new BeanWrapperImpl(object);
        for (Map.Entry<String, Object> entry : jobDataMap.entrySet()) {
            beanWrapper.setPropertyValue(entry.getKey(), entry.getValue());
        }
        return object;
    }

    @PostMapping("/scheduleEmail")
    public ResponseEntity<?> scheduleEmail(@Valid @RequestBody ScheduleEmailRequest scheduleEmailRequest) {

        try {
            ZonedDateTime dateTime = ZonedDateTime.of(scheduleEmailRequest.getDateTime(), scheduleEmailRequest.getTimeZone());
//            if(dateTime.isBefore(ZonedDateTime.now())) {
//                ScheduleEmailResponse scheduleEmailResponse = new ScheduleEmailResponse(false,
//                        "dateTime must be after current time");
//                return ResponseEntity.badRequest().body(scheduleEmailResponse);
//            }

            JobDetail jobDetail = buildJobDetail(scheduleEmailRequest);
            Trigger trigger = buildJobTriggerCron(jobDetail, dateTime);
            scheduler.scheduleJob(jobDetail, trigger);
//            Scheduler scheduler1 =
//            scheduler.tr

            ScheduleEmailResponse scheduleEmailResponse = new ScheduleEmailResponse(true,
                    jobDetail.getKey().getName(), jobDetail.getKey().getGroup(), "Email Scheduled Successfully!");
            return ResponseEntity.ok(scheduleEmailResponse);
        } catch (SchedulerException ex) {
            logger.error("Error scheduling email", ex);

            ScheduleEmailResponse scheduleEmailResponse = new ScheduleEmailResponse(false,
                    "Error scheduling email. Please try later!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(scheduleEmailResponse);
        }
    }

    private JobDetail buildJobDetail(Object scheduleEmailRequest) {
        JobDataMap jobDataMap = new JobDataMap();

        String[] nonNull = getNullPropertyNames(scheduleEmailRequest);
        BeanWrapper beanWrapper = new BeanWrapperImpl(scheduleEmailRequest);
        BeanWrapper beanWrapperMap = new BeanWrapperImpl(scheduleEmailRequest);
        for (String att : nonNull) {
            beanWrapper.setPropertyValue(att, beanWrapperMap.getPropertyValue(att));
            jobDataMap.put(att, beanWrapperMap.getPropertyValue(att));
        }

        try {
            ScheduleEmailRequest emailRequest = convertJobDataMapObject(jobDataMap, ScheduleEmailRequest.class);
            System.out.println("Done!");
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

//        jobDataMap.put("email", scheduleEmailRequest.getEmail());
//        jobDataMap.put("subject", scheduleEmailRequest.getSubject());
//        jobDataMap.put("body", scheduleEmailRequest.getBody());

        return JobBuilder.newJob(EmailJob.class)
                .withIdentity(UUID.randomUUID().toString(), "email-jobs")
                .withDescription("Send Email Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue != null && !pd.getName().equals("class")) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }


    private Trigger buildJobTrigger(JobDetail jobDetail, ZonedDateTime startAt) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "email-triggers")
                .withDescription("Send Email Trigger")
                .startAt(Date.from(startAt.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }

    private Trigger buildJobTriggerCron(JobDetail jobDetail, ZonedDateTime startAt) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "email-triggers")
                .withDescription("Send Email Trigger")
                .startAt(Date.from(startAt.toInstant()))
                .withSchedule(CronScheduleBuilder.cronSchedule("0 * * * * ?"))
                .build();
    }

    @GetMapping("/scheduleEmail")
    public ResponseEntity<ScheduleEmailResponse> scheduleEmail() throws SchedulerException {
//        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
//        Scheduler scheduler = schedulerFactory.getScheduler();

        // Define the job detail
        JobDetail jobDetail = JobBuilder.newJob(EmailJob.class)
                .withIdentity("26e75aca-0154-45a0-bc88-d66f5eecbf54", "email-jobs")
                .build();

//        // Define the trigger
        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity("scheduleEmail", "scheduleEmail")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(5)
                        .repeatForever())
                .build();
        Trigger trigger1 = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "email-triggers")
                .withDescription("Send Email Trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 * * * * ?"))
                .build();
//        List<Trigger> triggers = scheduler.get(jobDetail.getKey());
//        scheduler.rescheduleJob(triggers.get(0).getKey(), trigger);
//        scheduler.deleteJob(jobDetail.getKey());

        // Schedule the job with the trigger
        scheduler.scheduleJob(jobDetail, trigger);
        scheduler.scheduleJob(jobDetail, trigger1);
//        scheduler.
        // Start the scheduler
        scheduler.start();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/calendar")
    private void cal() throws SchedulerException {
//        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
//        Scheduler scheduler = schedulerFactory.getScheduler("calendar");
//        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

        // Create an AnnualCalendar to exclude weekends (Saturday and Sunday)
        AnnualCalendar calendar = new AnnualCalendar();
        Calendar excludedDate = new GregorianCalendar();
        excludedDate.set(Calendar.MONTH, Calendar.DECEMBER);
        excludedDate.set(Calendar.DAY_OF_MONTH, 25); // Christmas day
        excludedDate.set(Calendar.DAY_OF_MONTH, 21); // Christmas day
        calendar.setDayExcluded(excludedDate, true); // Exclude Christmas day
        scheduler.addCalendar("new cal", calendar, true, true);

        // Define the job and trigger
        JobDetail jobDetail = JobBuilder.newJob(EmailJob.class)
                .withIdentity("myJob-1", "group1")
                .storeDurably()
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("myTrigger-1", "group1")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(10)
                        .repeatForever())
                .modifiedByCalendar("new cal") // Apply the calendar to the trigger
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        scheduler.start();
    }

    @GetMapping("/custom")
    private void custom() throws SchedulerException {
//        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
//        Scheduler scheduler = schedulerFactory.getScheduler("calendar");
//        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

        // Define the job and trigger

        CustomTrigger customTrigger = new CustomTrigger();
        customTrigger.setName("customTrigger");
        customTrigger.setGroup("customGroup");
        customTrigger.setCustomProperty("customPropertyValue");

        // Schedule job with custom trigger
        JobDetail jobDetail = JobBuilder.newJob(EmailJob.class)
                .withIdentity("customTrigger", "customTrigger-g")
                .storeDurably()
                .build();

        scheduler.scheduleJob(jobDetail, customTrigger);
        scheduler.start();
    }

    @GetMapping("/pause")
    private void pause() throws SchedulerException {
//        scheduler.pauseTriggers(GroupMatcher.triggerGroupEquals("group1"));
        scheduler.resumeTriggers(GroupMatcher.triggerGroupEquals("group1"));
    }

    @GetMapping("/sim")
    private void triggerExample() throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(EmailJob.class)
                .withIdentity("triggerExample", "group1")
                .build();

        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(10)
                .repeatForever();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("triggerExample-myTrigger", "group1")
                .withSchedule(scheduleBuilder)
                .startNow()
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        scheduler.start();
    }
}
