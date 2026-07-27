package io.github.qlrxn1152.footballv3.teammember.service;

public interface TeamMemberService {

    void leaveTeam(Long teamId, Long memberId);

    void kickTeamMember(Long teamId, Long targetMemberId, Long loginMemberId);
}
