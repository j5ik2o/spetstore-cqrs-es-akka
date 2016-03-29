# SpetStore の CQRS+ES版(開発中)

## レイヤー構成

ヘキサゴナルアーキテクチャに準拠

## スタック構成

- コマンド側
    - write-interface
    - write-use-case
    - domain
- クエリ側
    - read-interface
    - read-use-case
- 共通インフラストラクチャ
　　- infrastructure

## ポートの定義

- ポートは各層に配置する。
- publicなtrait, classはポートになり得る。
