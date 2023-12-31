//******************************************************************************************************
// SessionLoginDTO.java
//
// Session login DTO file..
//
//Copyright (C) 2022, PICMG.
//
//        This program is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        This program is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with this program.  If not, see <https://www.gnu.org/licenses/>.
//*******************************************************************************************************

package org.picmg.redfish_server_template.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SessionLoginDTO {

    @JsonProperty("UserName")
    String UserName;
    @JsonProperty("Password")
    String Password;

    public SessionLoginDTO() {
    }

//    public SessionLoginDTO(String UserName, String Password) {
//        this.UserName = UserName;
//        this.Password = Password;
//    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
