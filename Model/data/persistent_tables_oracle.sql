
DROP SEQUENCE wdkstorage2.user_datasets_pkseq;
DROP SEQUENCE wdkstorage2.dataset_indices_pkseq;
DROP SEQUENCE wdkstorage2.answers_pkseq;

DROP SEQUENCE userlogins4.steps_pkseq;
DROP SEQUENCE userlogins4.strategies_pkseq;
DROP SEQUENCE userlogins4.users_pkseq;

DROP TABLE userlogins4.strategies;
DROP TABLE userlogins4.steps;
DROP TABLE userlogins4.user_datasets;
DROP TABLE userlogins4.preferences;
DROP TABLE userlogins4.user_roles;
DROP TABLE userlogins4.users;

DROP TABLE wdkstorage2.answer;
DROP TABLE wdkstorage2.clob_values;
DROP TABLE wdkstorage2.dataset_values;
DROP TABLE wdkstorage2.dataset_indices;



/* =========================================================================
   create sequences
   ========================================================================= */


CREATE SEQUENCE wdkstorage2.dataset_indices_pkseq INCREMENT BY 1 START WITH 1;

GRANT select ON wdkstorage2.dataset_indices_pkseq TO GUS_W;
GRANT select ON wdkstorage2.dataset_indices_pkseq TO GUS_R;


CREATE SEQUENCE wdkstorage2.answers_pkseq INCREMENT BY 1 START WITH 1;

GRANT select ON wdkstorage2.answers_pkseq TO GUS_W;
GRANT select ON wdkstorage2.answers_pkseq TO GUS_R;


CREATE SEQUENCE userlogins4.users_pkseq INCREMENT BY 1 START WITH 1;

GRANT select ON userlogins4.users_pkseq TO GUS_W;
GRANT select ON userlogins4.users_pkseq TO GUS_R;


CREATE SEQUENCE userlogins4.strategies_pkseq INCREMENT BY 1 START WITH 1;

GRANT select ON userlogins4.strategies_pkseq TO GUS_W;
GRANT select ON userlogins4.strategies_pkseq TO GUS_R;


CREATE SEQUENCE userlogins4.steps_pkseq INCREMENT BY 1 START WITH 1;

GRANT select ON userlogins4.steps_pkseq TO GUS_W;
GRANT select ON userlogins4.steps_pkseq TO GUS_R;


CREATE SEQUENCE userlogins4.user_datasets_pkseq INCREMENT BY 1 START WITH 1;

GRANT select ON userlogins4.user_datasets_pkseq TO GUS_W;
GRANT select ON userlogins4.user_datasets_pkseq TO GUS_R;



/* =========================================================================
   tables in wdk engine schema
   ========================================================================= */


CREATE TABLE wdkstorage2.answer
(
  answer_id NUMBER(12) NOT NULL,
  answer_checksum VARCHAR(40) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  project_version VARCHAR(50) NOT NULL,
  question_name VARCHAR(200) NOT NULL,
  query_checksum  VARCHAR(40) NOT NULL,
  estimate_size NUMBER(12),
  params CLOB,
  result_message CLOB,
  prev_answer_id NUMBER(12),
  CONSTRAINT "answer_pk" PRIMARY KEY (answer_id),
  CONSTRAINT "answer_uq1" UNIQUE (project_id, answer_checksum)
);

CREATE INDEX wdkstorage2.answer_idx01 ON wdkstorage2.answer (prev_answer_id);

GRANT insert, update, delete ON wdkstorage2.answer TO GUS_W;
GRANT select ON wdkstorage2.answer TO GUS_R;
GRANT references ON wdkstorage2.answer TO userlogins4;


CREATE TABLE wdkstorage2.dataset_indices
(
  dataset_id NUMBER(12) NOT NULL,
  dataset_checksum VARCHAR(40) NOT NULL,
  summary VARCHAR(200) NOT NULL,
  dataset_size NUMBER(12) NOT NULL,
  PREV_DATASET_ID NUMBER(12),
  CONSTRAINT "DATASET_INDICES_PK" PRIMARY KEY (dataset_id),
  CONSTRAINT "DATASET_CHECKSUM_UNIQUE" UNIQUE (dataset_checksum)
);

CREATE INDEX wdkstorage2.dataset_indices_idx01 ON wdkstorage2.dataset_indices (prev_dataset_id);

GRANT insert, update, delete ON wdkstorage2.dataset_indices TO GUS_W;
GRANT select ON wdkstorage2.dataset_indices TO GUS_R;
GRANT references ON wdkstorage2.dataset_indices TO userlogins4;


CREATE TABLE wdkstorage2.dataset_values
(
  dataset_id NUMBER(12) NOT NULL,
  dataset_value VARCHAR(4000) NOT NULL,
  CONSTRAINT "DATASET_VALUES_DATASET_ID_FK" FOREIGN KEY (dataset_id)
      REFERENCES wdkstorage2.dataset_indices (dataset_id)
);

CREATE INDEX wdkstorage2.dataset_values_idx01 ON wdkstorage2.dataset_values (dataset_id);

GRANT insert, update, delete ON wdkstorage2.dataset_values TO GUS_W;
GRANT select ON wdkstorage2.dataset_values TO GUS_R;


CREATE TABLE wdkstorage2.clob_values
(
  clob_checksum VARCHAR(40) NOT NULL,
  clob_value CLOB NOT NULL,
  CONSTRAINT "CLOB_VALUES_PK" PRIMARY KEY (clob_checksum)
);

GRANT insert, update, delete ON wdkstorage2.clob_values TO GUS_W;
GRANT select ON wdkstorage2.clob_values TO GUS_R;


/* =========================================================================
   tables in user schema
   ========================================================================= */
   
CREATE TABLE userlogins4.users
(
  user_id NUMBER(12) NOT NULL,
  email VARCHAR(255) NOT NULL,
  passwd VARCHAR(50) NOT NULL,
  is_guest NUMBER(1) NOT NULL,
  signature VARCHAR(40),
  register_time TIMESTAMP,
  last_active TIMESTAMP,
  last_name VARCHAR(50),
  first_name VARCHAR(50),
  middle_name VARCHAR(50),
  title VARCHAR(255),
  organization VARCHAR(255),
  department VARCHAR(255),
  address VARCHAR(500),
  city VARCHAR(255),
  state VARCHAR(255),
  zip_code VARCHAR(20),
  phone_number VARCHAR(50),
  country VARCHAR(255),
  PREV_USER_ID NUMBER(12),
  CONSTRAINT "USER_PK" PRIMARY KEY (user_id),
  CONSTRAINT "USER_EMAIL_UNIQUE" UNIQUE (email)
);

CREATE INDEX userlogins4.users_idx01 ON userlogins4.users (prev_user_id);

GRANT insert, update, delete ON userlogins4.users TO GUS_W;
GRANT select ON userlogins4.users TO GUS_R;


CREATE TABLE userlogins4.user_roles
(
  user_id NUMBER(12) NOT NULL,
  user_role VARCHAR(50) NOT NULL,
  CONSTRAINT "USER_ROLE_PK" PRIMARY KEY (user_id, user_role),
  CONSTRAINT "USER_ROLE_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins4.users (user_id) 
);

GRANT insert, update, delete ON userlogins4.user_roles TO GUS_W;
GRANT select ON userlogins4.user_roles TO GUS_R;


CREATE TABLE userlogins4.preferences
(
  user_id NUMBER(12) NOT NULL,
  project_id VARCHAR(50) NOT NULL,
  preference_name VARCHAR(200) NOT NULL,
  preference_value VARCHAR(4000),
  CONSTRAINT "PREFERENCES_PK" PRIMARY KEY (user_id, project_id, preference_name),
  CONSTRAINT "PREFERENCE_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins4.users (user_id) 
);

GRANT insert, update, delete ON userlogins4.preferences TO GUS_W;
GRANT select ON userlogins4.preferences TO GUS_R;


CREATE TABLE userlogins4.steps
(
  step_id NUMBER(12) NOT NULL,
  display_id NUMBER(12) NOT NULL,
  user_id NUMBER(12) NOT NULL,
  answer_id NUMBER(12) NOT NULL,
  left_child_id NUMBER(12),
  right_child_id NUMBER(12),
  create_time TIMESTAMP NOT NULL,
  last_run_time TIMESTAMP NOT NULL,
  estimate_size NUMBER(12),
  answer_filter VARCHAR(100),
  custom_name VARCHAR(4000),
  is_deleted NUMBER(1),
  collapsed_name varchar(200),
  is_collapsible NUMBER(1),
  display_params CLOB,
  CONSTRAINT "HISTORIES_PK" PRIMARY KEY (user_id, history_id),
  CONSTRAINT "HISTORY_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins4.users (user_id),
  CONSTRAINT "HISTORY_ANSWER_ID_FK" FOREIGN KEY (answer_id)
      REFERENCES wdkstorage2.answer (answer_id)
);

GRANT insert, update, delete ON userlogins4.histories TO GUS_W;
GRANT select ON userlogins4.histories TO GUS_R;


CREATE TABLE userlogins4.strategies
(
     strategy_id NUMBER(12) NOT NULL,
     display_id NUMBER(12) NOT NULL,
     user_id NUMBER(12) NOT NULL,
     root_step_id NUMBER(12) NOT NULL,
     project_id varchar(50) NOT NULL,
     is_saved NUMBER(1) NOT NULL,
     name varchar(200),
     CONSTRAINT "STRATEGIES_PK" PRIMARY KEY (strategy_id),
     CONSTRAINT "STRATEGIES_UNIQUE" UNIQUE (user_id, display_id, project_id),
     CONSTRAINT "STRATEGIES_STEP_FK" FOREIGN KEY (root_step_id, user_id, project_id)
         REFERENCES userlogins4.steps (display_id, user_id, project_id),
     CONSTRAINT "STRATEGIES_USER_ID_FK" FOREIGN KEY (user_id)
         REFERENCES userlogins4.users (user_id)
);

GRANT insert, update, delete on userlogins4.strategies to GUS_W;
GRANT select on userlogins4.strategies to GUS_R;


CREATE TABLE userlogins4.user_datasets
(
  user_dataset_id NUMBER(12) NOT NULL,
  dataset_id NUMBER(12) NOT NULL,
  user_id NUMBER(12) NOT NULL,
  create_time TIMESTAMP NOT NULL,
  upload_file VARCHAR(2000),
  CONSTRAINT "USER_DATASET_PK" PRIMARY KEY (user_dataset_id),
  CONSTRAINT "USER_DATASET_UQ1" UNIQUE (dataset_id, user_id),
  CONSTRAINT "USER_DATASETS_DS_ID_FK" FOREIGN KEY (dataset_id)
      REFERENCES wdkstorage2.dataset_indices (dataset_id),
  CONSTRAINT "USER_DATASETS_USER_ID_FK" FOREIGN KEY (user_id)
      REFERENCES userlogins4.users (user_id)
);

GRANT insert, update, delete ON userlogins4.user_datasets TO GUS_W;
GRANT select ON userlogins4.user_datasets TO GUS_R;
