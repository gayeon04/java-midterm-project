package com.hotmail.kalebmarc.textfighter.main;

import static java.util.Arrays.asList;

class Menu {

  public void load() {
    while (true) {

      //Menu Screen
      GameUtils.showPopup(Constants.WELCOME_HEADER,
          Constants.SUB_HEADER,
          asList("번호를 입력하고 엔터를 누르세요."),
          asList("게임 시작", "게임 소개")
      );

      switch (Ui.getValidInt()) {
        case 1:
          new Game().start();

          //Saves the game before exiting
          // docschorsch: save() only if player is not program default player amd game had started
          if (User.getPlayerDefault() > 0 && Game.hadGameStarted()) {
            Saves.save(false);
          }
          break;
        case 2:
          About.view(false);
          break;
        case 3:
          return;
        default:
          break;
      }
    }//Loop
  }//Method
}//Class
