# 매치 상세 조회

## API

GET /api/matches/{matchId}

## 공통 규칙

- 존재하는 매치만 조회할 수 있다.
- 로그인하지 않아도 조회할 수 있다.
- 홈팀 정보를 반환한다.
- 현재 팀 레이팅을 반환한다.
- 매치 상태를 반환한다.
- playedAt 을 반환한다.

## PENDING

- awayTeam 관련 값은 null이다.
- homeScore, awayScore는 null이다.
- winnerTeam 관련 값은 null이다.
- matchedAt은 null이다.
- completedAt은 null이다.

## MATCHED

- 홈팀과 원정팀 정보를 반환한다.
- homeScore, awayScore는 null이다.
- winnerTeam 관련 값은 null이다.
- matchedAt은 존재한다.
- completedAt은 null이다.

## COMPLETED

- 홈팀과 원정팀 정보를 반환한다.
- 경기 점수를 반환한다.
- 승리팀 정보를 반환한다.
- 무승부라면 winnerTeamId, winnerTeamName은 null이다.
- completedAt은 존재한다.

## 조회 실패

- 존재하지 않는 matchId라면 NotFoundTeamMatchException을 발생시킨다.

## 이번 범위 제외

- 득점자 기록
- 경기 수정/삭제
- 레이팅 이력
- 동시성