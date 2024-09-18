package com.chensoul.system.domain.notification.channel.mail;

public enum MailOauth2Provider {
  GOOGLE("Google"), OFFICE_365("Office 365"), SENDGRID("SendGrid"), CUSTOM("Custom");

  public final String label;

  MailOauth2Provider(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return label;
  }
}
