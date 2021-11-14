CREATE TABLE `VotingContext`
(
    `context`   json         NOT NULL,
    `id`        varchar(100) NOT NULL,
    `processed` tinyint(1) NOT NULL,
    `eid`       bigint       NOT NULL AUTO_INCREMENT,
    `state`     varchar(100) NOT NULL,
    PRIMARY KEY (`eid`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;