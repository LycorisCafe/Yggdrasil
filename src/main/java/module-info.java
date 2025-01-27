module Yggdrasil {
    requires com.google.gson;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires io.github.lycoriscafe.nexus.http;
    requires jdk.jshell;
    requires org.slf4j;

    opens io.github.lycoriscafe.yggdrasil.authentication;
    opens io.github.lycoriscafe.yggdrasil.commons;
    opens io.github.lycoriscafe.yggdrasil.configuration;
    opens io.github.lycoriscafe.yggdrasil.rest;
    opens io.github.lycoriscafe.yggdrasil.rest.admin;
    opens io.github.lycoriscafe.yggdrasil.rest.classroom;
    opens io.github.lycoriscafe.yggdrasil.rest.guardian;
    opens io.github.lycoriscafe.yggdrasil.rest.notification;
    opens io.github.lycoriscafe.yggdrasil.rest.relief;
    opens io.github.lycoriscafe.yggdrasil.rest.student;
    opens io.github.lycoriscafe.yggdrasil.rest.student.attendance;
    opens io.github.lycoriscafe.yggdrasil.rest.student.subject;
    opens io.github.lycoriscafe.yggdrasil.rest.subject;
    opens io.github.lycoriscafe.yggdrasil.rest.teacher;
    opens io.github.lycoriscafe.yggdrasil.rest.teacher.attendance;
    opens io.github.lycoriscafe.yggdrasil.rest.teacher.subject;
    opens io.github.lycoriscafe.yggdrasil.rest.timetable;
}