/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.dnd.blchat;

import kz.dnd.blchat.wrappers.ServerWrapper;

/**
 *
 * @author bakhyt
 */
public class Main {


    public static void main(String[] args) throws InterruptedException {
        ServerWrapper serverWrapper = new ServerWrapper();
        serverWrapper.initializeAndRunServer();

    }


}
