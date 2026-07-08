-- H2 Compatible Schema for Smart Farming System

-- 1. User table
CREATE TABLE IF NOT EXISTS sys_user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    barn_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_user_username ON sys_user(username);
CREATE INDEX IF NOT EXISTS idx_user_role ON sys_user(role);

-- 2. Environmental sensor data table
CREATE TABLE IF NOT EXISTS env_sensor_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sensor_id VARCHAR(50) NOT NULL,
    barn_id BIGINT NOT NULL,
    temperature DECIMAL(5,2),
    humidity DECIMAL(5,2),
    ammonia_level DECIMAL(5,2),
    timestamp TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sensor_time ON env_sensor_data(sensor_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_sensor_barn_time ON env_sensor_data(barn_id, timestamp);

-- 3. Animal behavior data table
CREATE TABLE IF NOT EXISTS animal_behavior (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    barn_id BIGINT NOT NULL,
    behavior_type VARCHAR(20) NOT NULL,
    confidence_score DECIMAL(5,4),
    timestamp TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_behavior_barn_time ON animal_behavior(barn_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_behavior_type ON animal_behavior(behavior_type);

-- 4. Anomaly event table
CREATE TABLE IF NOT EXISTS anomaly_event (
    event_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    barn_id BIGINT NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    severity_level VARCHAR(10) NOT NULL,
    description TEXT,
    timestamp TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_anomaly_barn_time ON anomaly_event(barn_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_anomaly_severity ON anomaly_event(severity_level);

-- 5. Causal relation table
CREATE TABLE IF NOT EXISTS causal_relation (
    relation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    barn_id BIGINT NOT NULL,
    cause_variable VARCHAR(50) NOT NULL,
    effect_variable VARCHAR(50) NOT NULL,
    correlation_score DECIMAL(5,4),
    causal_strength DECIMAL(5,4),
    timestamp TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_causal_barn_time ON causal_relation(barn_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_causal_cause_effect ON causal_relation(cause_variable, effect_variable);

-- 6. Traceability report table
CREATE TABLE IF NOT EXISTS traceability_report (
    report_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    barn_id BIGINT NOT NULL,
    cause_chain TEXT,
    report_content TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_report_event ON traceability_report(event_id);
CREATE INDEX IF NOT EXISTS idx_report_barn_time ON traceability_report(barn_id, create_time);

-- 7. Operation log table
CREATE TABLE IF NOT EXISTS operation_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    operation VARCHAR(100) NOT NULL,
    method VARCHAR(100),
    params TEXT,
    ip VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_log_user_time ON operation_log(user_id, create_time);

-- 8. Alarm record table
CREATE TABLE IF NOT EXISTS alarm_record (
    alarm_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    barn_id BIGINT NOT NULL,
    alarm_type VARCHAR(20) NOT NULL,
    alarm_level VARCHAR(10) NOT NULL,
    alarm_content TEXT,
    status VARCHAR(20) DEFAULT 'pending',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    handle_time TIMESTAMP,
    handler_id BIGINT
);
CREATE INDEX IF NOT EXISTS idx_alarm_barn_time ON alarm_record(barn_id, create_time);
CREATE INDEX IF NOT EXISTS idx_alarm_status ON alarm_record(status);

-- 9. System config table
CREATE TABLE IF NOT EXISTS system_config (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(200),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_config_key ON system_config(config_key);

-- Insert default admin user (password: admin123)
INSERT INTO sys_user (username, password, role) VALUES ('admin', '$2b$12$IpuLdOxVDHgpk/k.DYls2e77o/g9nUn.4VjSM2Nz2fqC7hEQmgKIm', 'admin');

-- Insert default threshold configurations
INSERT INTO system_config (config_key, config_value, description) VALUES
('temperature_max', '35.00', 'Temperature upper limit'),
('temperature_min', '15.00', 'Temperature lower limit'),
('humidity_max', '85.00', 'Humidity upper limit'),
('humidity_min', '40.00', 'Humidity lower limit'),
('ammonia_level_max', '25.00', 'Ammonia concentration upper limit');
