package com.fred.springbatch_demo;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.UUID;

@SpringBootApplication
public class SpringBatchDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchDemoApplication.class, args);
	}

	@Bean
	ApplicationRunner runner(JobLauncher jobLauncher, Job job){
		return args -> {
			JobParameters jobParameters = new JobParametersBuilder()
					.addString("uuid", UUID.randomUUID().toString())
					.toJobParameters();
			JobExecution run = jobLauncher.run(job, jobParameters);
			long instanceId = run.getJobInstance().getInstanceId();
			System.out.println("instanceId: "+instanceId);
		};
	}

	@Bean
	@StepScope
	Tasklet tasklet(@Value("#{jobParameters['uuid']}") String uuid){

		return  (contribution, chunkContext) ->{
			System.out.println("Hello,Spring batch！UUID is "+ uuid);
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	Job job(JobRepository repository, Step step){
		return  new JobBuilder("job",repository)
				.start(step)
				.build();
	}

	@Bean
	Step step(JobRepository repository, Tasklet tasklet, PlatformTransactionManager transactionManager){
		return new StepBuilder("step",repository)
				.tasklet(tasklet,transactionManager)
				.build();
	}
}
