/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.dnd.blchat.common;

import java.util.List;

/**
 *
 * @author bakhyt
 */
public class SyncUsers {

    List<String> phones;

    public List<String> getPhones() {
        return phones;
    }

    public void setPhones(List<String> phones) {
        this.phones = phones;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (phones != null && !phones.isEmpty()) {
            String delim = "";
            for (String phone : phones) {
                sb.append(delim).append(phone);
                delim = ",";
            }
        }

        return "SyncUsers{" + "phones=[" + sb.toString() + "]}";
    }

}
