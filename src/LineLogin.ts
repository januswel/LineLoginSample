import {NativeModules} from 'react-native';

const {LineLogin} = NativeModules;

export interface Profile {
  idToken: string;
  displayName: string;
  pictureUrl: string;
  email: string;
}
export type Login = (nonce: string) => Promise<Profile>;
export type Logout = () => Promise<void>;

export const login: Login = LineLogin.login;
export const logout: Logout = async function () {
  await LineLogin.logout('dummy');
};
