1. 존재하는 매치여야 한다.

2. 로그인 Member가 존재해야 한다.

3. MATCHED 상태만 결과를 등록할 수 있다.

4. 홈팀 팀장만 결과를 등록할 수 있다.

5. 경기 결과는 한 번만 등록할 수 있다.

6. homeScore / awayScore는 0 이상이어야 한다.

7. homeScorers의 goalCount 합계
   = homeScore

8. awayScorers의 goalCount 합계
   = awayScore

9. 각 득점자의 goalCount는 1 이상이어야 한다.

10. 홈팀 득점자는 실제 홈팀 소속 선수여야 한다.

11. 원정팀 득점자는 실제 원정팀 소속 선수여야 한다.

12. 같은 선수를 한 경기에서 두 번 입력할 수 없다.

13. 같은 선수를 homeScorers와 awayScorers 양쪽에 넣을 수 없다.

14. 0골인 팀은 scorer 목록이 빈 배열이어야 한다.

15. 0:0은
    homeScorers=[]
    awayScorers=[]
    로 정상 결과 등록 가능하다.

16. 결과 저장 성공 시 Member.totalGoalCount도 함께 증가한다.

17. 모든 처리는 하나의 트랜잭션에서 수행한다.

18. 검증 하나라도 실패하면
    - TeamMatch COMPLETED 변경 X
    - TeamMatchResult 저장 X
    - TeamMatchGoal 저장 X
    - Team Rating 변경 X
    - Member.totalGoalCount 변경 X