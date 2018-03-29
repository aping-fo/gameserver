package com.game.sdk.erating.domain;

import com.game.sdk.erating.NodeName;

/**
 * Created by lucky on 2018/2/1.
 */
@NodeName(name = "subject_info")
public class SubjectInfo extends Report {
    @NodeName(name = "subject_id")
    public final int subjectId;
    @NodeName(name = "sub_amount")
    public final int subAmount;

    public SubjectInfo(int subjectId, int subAmount) {
        this.subjectId = subjectId;
        this.subAmount = subAmount;
    }
}
