ALTER TABLE `meet_xxl_job_admin`.`xxl_job_group`
MODIFY COLUMN `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '执行器名称' AFTER `app_name`;
ALTER TABLE `meet_xxl_job_admin`.`xxl_job_group`
ADD UNIQUE INDEX `U_APP_NAME`(`app_name`);

ALTER TABLE `meet_xxl_job_admin`.`xxl_job_info`
ADD UNIQUE INDEX `u_executor_handler`(`executor_handler`);