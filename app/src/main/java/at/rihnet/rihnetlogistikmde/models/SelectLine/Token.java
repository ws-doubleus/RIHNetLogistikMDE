package at.rihnet.rihnetlogistikmde.models.SelectLine;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Token {
    @JsonProperty("AccessToken")
    private String AccessToken;
    @JsonProperty("TokenType")
    private String TokenType;

    public Token(){

    }

    public Token(String accessToken, String tokenType) {
        this.AccessToken = accessToken;
        this.TokenType = tokenType;
    }

    public String getAccessToken() {
        return AccessToken;
    }

    public void setAccessToken(String accessToken) {
        this.AccessToken = accessToken;
    }

    public String getTokenType() {
        return TokenType;
    }

    public void setTokenType(String tokenType) {
        this.TokenType = tokenType;
    }
}
