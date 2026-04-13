package com.sports.NetsCricket.config;

//@Configuration
//@EnableWebSecurity
public class SecurityConfig1 {
	
	/*@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
		
		httpSecurity.csrf(Customizer -> Customizer.disable());//csrf token
		httpSecurity.authorizeHttpRequests(request -> request.anyRequest().authenticated());//disabling access for all
		//httpSecurity.formLogin(Customizer.withDefaults());// login form
		httpSecurity.httpBasic(Customizer.withDefaults());//rest access
		httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));//state less it will give login form every time but it is works in postman
		
		return httpSecurity.build();
	}*/
	
	/* manually giving credentials
	@Bean
	public UserDetailsManager userDetailsManager() {
		
		UserDetails user1 = User.withDefaultPasswordEncoder()
				.username("vamsi")
				.password("vamsi").roles("ADMIN").build();
		
		UserDetails user2 = User.withDefaultPasswordEncoder()
				.username("chandu")
				.password("chandu").roles("ADMIN").build();
		
		return new InMemoryUserDetailsManager(user1,user2);
	}*/

}
