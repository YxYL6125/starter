package com.yxyl.schedule.annotation;

import com.yxyl.schedule.DoJoinPoint;
import com.yxyl.schedule.config.DcsSchedulingConfiguration;
import com.yxyl.schedule.task.CronTaskRegister;
import com.yxyl.schedule.task.SchedulingConfig;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author YxYL
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({DcsSchedulingConfiguration.class})
@ImportAutoConfiguration({SchedulingConfig.class, CronTaskRegister.class, DoJoinPoint.class})
@ComponentScan("com.yxyl.schedule.*")
public @interface EnableDcsScheduling {
}
