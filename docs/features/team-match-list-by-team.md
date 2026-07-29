# 특정 팀의 상태별 매치 목록 조회

## 기능 목적

- 팀 상세 화면에서 특정 팀이 참가한 매치를 상태별로 조회한다.

## API

- GET /api/teams/{teamId}/matches/pending
- GET /api/teams/{teamId}/matches/matched
- GET /api/teams/{teamId}/matches/completed

## 공통 규칙

- 존재하는 팀만 조회할 수 있다.
- 로그인하지 않아도 조회할 수 있다.
- 조회 결과가 없으면 빈 배열을 반환한다.
- 다른 팀의 경기는 포함하지 않는다.

## PENDING

- PENDING 상태만 반환한다.
- 해당 팀이 홈팀으로 등록한 매치만 반환한다.
- 경기 예정 시간이 가까운 순서로 정렬한다.

## MATCHED

- MATCHED 상태만 반환한다.
- 해당 팀이 홈팀 또는 원정팀인 경기를 반환한다.
- 경기 예정 시간이 가까운 순서로 정렬한다.

## COMPLETED

- COMPLETED 상태만 반환한다.
- 해당 팀이 홈팀 또는 원정팀인 경기를 반환한다.
- 점수와 승리팀 정보를 반환한다.
- 무승부도 목록에 포함한다.
- 최근 완료된 경기부터 반환한다.

## 정렬

### PENDING / MATCHED

- playedAt ASC
- createdAt ASC
- matchId ASC

### COMPLETED

- completedAt DESC
- matchId DESC