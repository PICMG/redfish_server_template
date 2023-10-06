package org.picmg.redfish_server_template.RFmodels.custom;

import org.picmg.redfish_server_template.RFmodels.AllModels.Odata_IdRef;
import java.util.*;

public interface RedfishCollection {
    public List<Odata_IdRef> getMembers();
    public void setMembers(List<Odata_IdRef> members);
    public Long getMembersAtOdataCount();
    public void setMembersAtOdataCount(Long membersAtOdataCount);
    public String getMembersAtOdataNextLink();
    public void setMembersAtOdataNextLink(String membersAtOdataNextLink);
}
