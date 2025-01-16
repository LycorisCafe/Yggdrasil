module Yggdrasil {
    requires com.google.gson;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires nexus.http;

    opens io.github.lycoriscafe.yggdrasil.rest.admin;
    opens io.github.lycoriscafe.yggdrasil.rest.classroom;
    opens io.github.lycoriscafe.yggdrasil.rest.guardian;
    opens io.github.lycoriscafe.yggdrasil.rest.notification;
    opens io.github.lycoriscafe.yggdrasil.rest.relief;
    opens io.github.lycoriscafe.yggdrasil.rest.student;
    opens io.github.lycoriscafe.yggdrasil.rest.subject;
    opens io.github.lycoriscafe.yggdrasil.rest.teacher;
    opens io.github.lycoriscafe.yggdrasil.rest.timetable;

    exports io.github.lycoriscafe.yggdrasil;
    opens io.github.lycoriscafe.yggdrasil.rest.student.attendance;
}