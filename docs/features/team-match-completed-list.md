# COMPLETED 매치 목록 조회

## 기능 목적

- 경기 결과 입력이 완료된 매치들을 조회한다.
- 홈팀과 원정팀의 경기 결과를 확인할 수 있다.

## 비즈니스 규칙

- TeamMatchStatus.COMPLETED 상태인 매치만 조회한다.
- 홈팀과 원정팀 정보를 반환한다.
- 홈팀과 원정팀의 경기 점수를 반환한다.
- 승리팀 정보를 반환한다.
- 무승부인 경우 winnerTeamId, winnerTeamName은 null이다. (left fetch join 을 사용한다.)
- 최근 완료된 경기부터 조회한다.
- 조회 결과가 없으면 빈 배열을 반환한다.
- 로그인 여부와 관계없이 조회할 수 있다.

## 정렬

- completedAt DESC
- completedAt이 같으면 matchId DESC

## 처리 순서

1. COMPLETED 상태의 TeamMatchResult를 조회한다.
2. TeamMatch를 함께 조회한다.
3. homeTeam, awayTeam을 함께 조회한다.
4. winnerTeam을 함께 조회한다.
5. TeamMatchCompletedResponse로 변환한다.
6. 완료 시간이 최근인 순서로 반환한다.

## API

GET /api/matches/completed