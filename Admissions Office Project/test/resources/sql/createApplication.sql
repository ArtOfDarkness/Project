DELETE FROM rating_list;
DELETE FROM zno_marks;
DELETE FROM application;
DELETE FROM speciality;
DELETE FROM subject_faculty;
DELETE FROM faculty;
DELETE FROM subject;
DELETE FROM applicant;

INSERT INTO applicant VALUES
(2, '2000-11-15', 'Чернигов', '№1', '', 'application/octet-stream', x'');

INSERT INTO subject VALUES
(1, 'Украинский язык'),
(2, 'История Украины'),
(3, 'Математика');

INSERT INTO faculty VALUES
(1, 'Физико-математический');

INSERT INTO subject_faculty VALUES
(1, 1),
(1, 2),
(1, 3);

INSERT INTO speciality VALUES
(1, 'Фінанси та кредит', 1, 4, FALSE),
(2, 'Облік і аудит', 1, 5, FALSE),
(3, 'Міжнародні відносини', 1, 8, FALSE);

INSERT INTO application VALUES
(1, 2, 1, 175),
(2, 2, 2, 175),
(3, 2, 3, 175);

INSERT INTO zno_marks VALUES
(1, 1, 172),
(1, 2, 167),
(1, 3, 159),
(2, 1, 172),
(2, 2, 167),
(2, 3, 159),
(3, 1, 172),
(3, 2, 167),
(3, 3, 159);

INSERT INTO rating_list VALUES
(1, 168.25, FALSE, NULL),
(2, 168.25, FALSE, NULL),
(3, 168.25, FALSE, NULL);