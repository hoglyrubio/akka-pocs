package com.hogly.cluster.multitenancy;

import akka.actor.ActorSystem;
import com.hogly.cluster.multitenancy.admin.AdminController;
import com.hogly.cluster.multitenancy.admin.AdminService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class App {

  public static void main(String[] args) {
    Config adminConfig = ConfigFactory.load("multitenancy-admin.conf");
    ActorSystem adminSystem = ActorSystem.create("ADMIN", adminConfig);

    AdminService adminService = new AdminService(adminSystem);
    AdminController adminController = new AdminController(adminSystem, adminConfig.getConfig("multitenancy.http"), adminService);
    adminController.start();
  }

}
