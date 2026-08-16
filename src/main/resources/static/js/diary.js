 /**
  * 日記アプリ共通JavaScript。
  * ユーザー登録・日記登録・日記編集に関する通知表示と、
  * 日記編集モーダルの非同期処理を一元管理します。
  */

 /**
  * 共通通知モーダルを表示します。
  *
  * @param {string} message 表示するメッセージ
  * @param {string} type OKボタン押下後の動作種別
  */
 function showNotification(message, type = "reload") {
     const modal = document.getElementById("notificationModal");
     const messageElement = document.getElementById("notificationMessage");
     const okButton = document.getElementById("notificationOkButton");

     if (!modal || !messageElement || !okButton) {
         console.error("通知モーダルのHTML要素が見つかりません。");
         return;
     }

     // メッセージをHTMLとして解釈させず、安全にテキストとして表示します。
     messageElement.textContent = message;

     // 通知モーダルを表示します。
     modal.style.display = "flex";
     modal.setAttribute("aria-hidden", "false");

     // 通知の種類に応じてOKボタン押下後の処理を切り替えます。
     okButton.onclick = function () {
         modal.style.display = "none";
         modal.setAttribute("aria-hidden", "true");

         // ユーザー登録成功後はログイン画面へ遷移します。
         if (type === "login") {
             window.location.href = "/login";
             return;
         }

         // ユーザー登録エラー時は登録画面へ戻します。
         if (type === "register") {
             window.location.href = "/register";
             return;
         }

         // 入力エラー時は現在の画面を維持します。
         // 再読み込みによるPOST再送信や通知ループを防止します。
         if (type === "stay") {
             return;
         }

         // 日記登録・更新成功時は最新状態を表示するため再読み込みします。
         window.location.reload();
     };
 }

 /**
  * ページ読み込み完了後にサーバーから渡された通知メッセージを確認し、
  * 該当するメッセージが存在する場合は共通通知モーダルを表示します。
  */
 document.addEventListener("DOMContentLoaded", function () {
     const registerSuccessMessage = document.getElementById("registerSuccessMessage");
     const registerErrorMessage = document.getElementById("registerErrorMessage");
     const diaryErrorMessage = document.getElementById("diaryErrorMessage");
     const diarySuccessMessage = document.getElementById("diarySuccessMessage");

     // ユーザー登録成功時は通知後にログイン画面へ移動します。
     if (registerSuccessMessage) {
         showNotification(registerSuccessMessage.textContent, "login");
         return;
     }

     // ユーザー登録エラー時は通知後に新規登録画面へ戻します。
     if (registerErrorMessage) {
         showNotification(registerErrorMessage.textContent, "register");
         return;
     }

     // 日記登録エラー時は通知だけを閉じ、入力内容を保持した画面に留まります。
     if (diaryErrorMessage) {
         showNotification(diaryErrorMessage.textContent, "stay");
         return;
     }

     // 日記登録成功時は通知後に画面を再読み込みします。
     if (diarySuccessMessage) {
         showNotification(diarySuccessMessage.textContent, "reload");
     }
 });

 /**
  * 編集対象の日記を取得し、編集モーダルへ内容を設定します。
  *
  * @param {HTMLElement} button 編集ボタン
  */
 async function openEditModal(button) {
     const id = button.getAttribute("data-id");
     const modal = document.getElementById("editModal");
     const titleInput = document.getElementById("editTitle");
     const contentInput = document.getElementById("editContent");
     const editForm = document.getElementById("editForm");
     const errorMessage = document.getElementById("editErrorMessage");

     try {
         const response = await fetch(`/api/diary/${id}`);

         if (!response.ok) {
             showNotification("日記を取得できませんでした。", "stay");
             return;
         }

         const diary = await response.json();

         // APIから取得した日記情報を編集フォームへ設定します。
         titleInput.value = diary.title;
         contentInput.value = diary.content;
         editForm.dataset.id = diary.id;
         errorMessage.textContent = "";

         // 編集モーダルを表示します。
         modal.style.display = "block";
         modal.setAttribute("aria-hidden", "false");
     } catch (error) {
         console.error("日記の取得中にエラーが発生しました。", error);
         showNotification("日記の取得中にエラーが発生しました。", "stay");
     }
 }

 /**
  * 日記編集フォームを非同期で送信し、更新結果を通知します。
  */
 const editForm = document.getElementById("editForm");

 if (editForm) {
     editForm.addEventListener("submit", async function (event) {
         event.preventDefault();

         const id = editForm.dataset.id;
         const title = document.getElementById("editTitle").value;
         const content = document.getElementById("editContent").value;
         const csrfToken = document.getElementById("csrfToken").value;

         const formData = new URLSearchParams();
         formData.append("title", title);
         formData.append("content", content);

         try {
             const response = await fetch(`/api/diary/${id}`, {
                 method: "POST",
                 headers: {
                     "Content-Type": "application/x-www-form-urlencoded",
                     "X-CSRF-TOKEN": csrfToken
                 },
                 body: formData
             });

             if (!response.ok) {
                 // サーバーから返されたバリデーションエラーを共通通知ポップアップで表示します。
                 const message = await response.text();
                 showNotification(message, "stay");
                 return;
             }

             // 更新成功後は編集モーダルを閉じ、完了通知を表示します。
             closeEditModal();
             showNotification(`日記「${title}」を更新しました。`, "reload");
         } catch (error) {
             console.error("日記の更新中にエラーが発生しました。", error);
             showNotification("日記の更新中にエラーが発生しました。", "stay");
         }
     });
 }

 /**
  * 日記編集モーダルを閉じます。
  */
 function closeEditModal() {
     const modal = document.getElementById("editModal");

     if (!modal) {
         return;
     }

     // モーダルを非表示にし、アクセシビリティ属性も非表示状態へ戻します。
     modal.style.display = "none";
     modal.setAttribute("aria-hidden", "true");
 }