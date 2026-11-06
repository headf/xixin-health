ALTER TABLE t_exam_item ADD COLUMN review_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED';
ALTER TABLE t_exam_item ADD COLUMN reviewer_id BIGINT;
ALTER TABLE t_exam_item ADD COLUMN reviewed_at TIMESTAMP;

ALTER TABLE t_business_rule ADD COLUMN review_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED';
ALTER TABLE t_business_rule ADD COLUMN reviewer_id BIGINT;
ALTER TABLE t_business_rule ADD COLUMN reviewed_at TIMESTAMP;

ALTER TABLE t_report_template ADD COLUMN review_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED';
ALTER TABLE t_report_template ADD COLUMN reviewer_id BIGINT;
ALTER TABLE t_report_template ADD COLUMN reviewed_at TIMESTAMP;
