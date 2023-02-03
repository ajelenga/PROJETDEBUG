module debugger.project {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.swing;
    requires javafx.web;
    requires javafx.swt;
    requires jdk.jdi;

    opens jfx.sample;
    opens jfx.sample.controller;
}