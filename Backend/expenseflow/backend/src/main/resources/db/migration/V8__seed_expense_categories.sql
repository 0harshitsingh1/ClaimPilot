INSERT INTO expense_category (name, description) VALUES
('Travel', 'Travel expenses including flights, trains, and taxis'),
('Food and meals', 'Meals and food expenses during business trips or client meetings'),
('Accommodation', 'Hotel and accommodation expenses'),
('Fuel', 'Vehicle fuel costs'),
('Office supplies', 'Stationery, office supplies, and equipment'),
('Internet and mobile', 'Internet bills and mobile recharge expenses'),
('Training', 'Professional training, courses, and certifications'),
('Client entertainment', 'Entertainment expenses for clients'),
('Other', 'Miscellaneous business expenses')
ON CONFLICT (name) DO NOTHING;

INSERT INTO app_user (full_name, email, password, employee_code, role, is_active, enabled)
VALUES ('Employee User', 'employee@expenseflow.com', '$2a$10$PlQrsA2CpAW6p7zE0wJWC.Eb49Oj7va2ARgXJDsG2v5RuUympA09S', 'EMP002', 'EMPLOYEE', TRUE, TRUE)
ON CONFLICT (email) DO NOTHING;
