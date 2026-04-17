package demo.vikram.springai.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApplicationProperties applicationProperties;

    @Bean
    @Order(1)
    public SecurityFilterChain authServerFilterChain(HttpSecurity httpSecurity, AuthorizationServerSettings authServerSettings) {

        log.info("Configuring auth server filter config.....");
        OAuth2AuthorizationServerConfigurer oAuth2AuthorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        httpSecurity
                .csrf(CsrfConfigurer::disable)
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                .oauth2AuthorizationServer((authorizationServer) -> {
                    httpSecurity.securityMatcher(authorizationServer.getEndpointsMatcher());
                    authorizationServer
                            .oidc(Customizer.withDefaults());    // Enable OpenID Connect 1.0
                })
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .requestMatchers(createOidcPromptNoneMatcher(authServerSettings)).permitAll()
                                .anyRequest().authenticated()
                )
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );

        return httpSecurity.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity httpSecurity) {

        httpSecurity
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                        authorizationManagerRequestMatcherRegistry.anyRequest().authenticated())
                .csrf(CsrfConfigurer::disable)
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                .formLogin(Customizer.withDefaults());

        return httpSecurity.build();
    }

    static CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPaid = generateRsaKey();
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPaid.getPublic();
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPaid.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(rsaPublicKey)
                .privateKey(rsaPrivateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public UserDetailsService users() {
        List<UserDetails> userDetailsList = applicationProperties.getCredentials().keySet().stream()
                .map(username ->
                        User.builder()
                                .username(username)
                                .password("{noop}" + applicationProperties.getCredentials().get(username))
                                .roles("HOBBIT")
                                .build()
                ).toList();
        return new InMemoryUserDetailsManager(userDetailsList);
    }

    private static RequestMatcher createOidcPromptNoneMatcher(AuthorizationServerSettings authServerSettings) {
        String authorizationEndpointUri = authServerSettings.getAuthorizationEndpoint();
        RequestMatcher authorizationRequestGetMatcher = PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.GET, authorizationEndpointUri);
        RequestMatcher authorizationRequestPostMatcher = PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.POST, authorizationEndpointUri);
        RequestMatcher oidcPromptNoneMatcher = request ->
                "none".equals(request.getParameter("prompt"));
        return new AndRequestMatcher(
                new OrRequestMatcher(authorizationRequestGetMatcher, authorizationRequestPostMatcher),
                oidcPromptNoneMatcher
        );
    }

    @Bean
    Customizer<HttpSecurity> mcpInspectorCustomizations() {
        return http -> {
            http.csrf(CsrfConfigurer::disable);
            http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        };
    }
}