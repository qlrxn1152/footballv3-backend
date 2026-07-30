1. 존재하는 경기여야 한다.

2. COMPLETED 경기만 득점자를 등록할 수 있다.

3. 홈팀 팀장만 등록할 수 있다.

4. 경기 결과가 반드시 존재해야 한다.

5. 홈팀 득점자 골 합계 = homeScore

6. 원정팀 득점자 골 합계 = awayScore

7. 득점자는 실제 해당 경기의 홈팀/원정팀 소속 선수여야 한다.

8. 선수 한 명당 한 경기에서 한 행만 저장한다.

9. 같은 선수를 request 안에서 중복 등록할 수 없다.

10. goalCount는 1 이상이어야 한다.

11. 0:0 경기는
    homeScorers = []
    awayScorers = []
    로 정상 등록 가능하다.

12. 득점 기록 성공 시
    Member.totalGoalCount도 같이 증가한다.

13. 동일 경기의 득점 기록은 한 번만 가능하다.

14. 중간에 검증 하나라도 실패하면
    TeamMatchGoal 저장 X
    Member.totalGoalCount 변경 X