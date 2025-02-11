# AzipediaSearcher

![スクリーンショット 2025-02-11 155417](https://github.com/user-attachments/assets/9aab2ec7-1384-48a3-bd3e-5dea35af9bf9)

## 概要
MediaWiki内のページをマインクラフト内から検索するプラグイン

デフォルトではAzipediaが指定されていますが、MediaWikiであれば設定で変更可能です

## 導入
1：Releasesから最新のjarファイルをダウンロード

2：サーバーのpluginsフォルダに入れる

3：Plugmanを利用してロードするか、サーバーを再起動する。

4：AzipediaSearcherフォルダ内にあるconfig.ymlを設定する。

5：`/wiki reload`でConfigを再読み込みするか、サーバーを再起動する。

## コマンド
### 全般
・`/wiki help`: AzipediaSearcherのヘルプ

・`/wiki reload`: Configの再読み込み

・`/wiki version`: バージョンの表示

・`/wiki search <検索する単語> <検索オプション>`

#### 検索オプション
・検索する単語は複数指定できます

・type=orでどちらかが当てはまる場合の検索結果を表示します(デフォルトではtype=and)

・limit=titleでタイトルのみを検索します(デフォルトではtype=text)

![スクリーンショット 2025-02-11 155403](https://github.com/user-attachments/assets/5aee984e-b91a-4bd4-875e-3757a7a48b68)


エイリアス: `/azipedia`,`azisabawiki`,`w`

## 権限 / Permission
- `azipediasearcher.command.reload`: reloadコマンド権限

## ライセンス / License
[MIT License](LICENSE)
