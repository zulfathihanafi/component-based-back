import { useCookies } from 'react-cookie';
import { useNavigate } from "react-router-dom";

import { HoverButton } from '../commons';

export function ClientSearchBar(props) {


      return (
            <div style={outerContainer}>
                  <div style={actionButtonContainer}>
                  <HoverButton text="ADD" HoverStyle={ActionButtonHover} DefaultStyle={ActionButton} onClick={props.onAddClick} />
                  </div>
                  <div style={searchNavContainer}>
                    <input placeholder='First Name or Last Name' style={Input}/>
                    <input placeholder='User ID' style={Input}/>
                    <HoverButton text="SEARCH" HoverStyle={SearchButtonHover} DefaultStyle={SearchButton} />
                  </div>
            
            </div>
      );
}


const outerContainer = {
    "width": "90%",
    "margin": "auto",
    "height": "fit-content",
    "padding": "20px",
    "borderBottom": "1px solid #2d3b55",
    
}

const actionButtonContainer = {
    "display": "inline-block",
    "margin-left": "20px",
    "vertical-align": "middle",

}

const searchNavContainer = {
    "display": "inline-block",
    "float": "right",
    "margin-right": "20px",
    "vertical-align": "middle",
}


const ActionButton = {
    "border": "none",
    "padding": "18px",
    "padding-top": "10px",
    "padding-bottom": "10px",
    "font-family": "'Rubik', sans-serif",
    "cursor": "pointer",
    "text-transform": "uppercase",
    "font-weight": "550",
    "background": "#2d3b55",
    "color": "#fff",
    "border-bottom-left-radius": "4px",
    "border-bottom-right-radius": "0",
    "letter-spacing": "0.2px",
    "outline": "0",
    "-webkit-transition": "all .3s",
    "transition": "all .3s",
}

const ActionButtonHover = {
    
    "background": "#3c4d6d",

}



const SearchButton = {
    "border": "none",
    "padding": "18px",
    "padding-top": "10px",
    "padding-bottom": "10px",
    "font-family": "'Rubik', sans-serif",
    "cursor": "pointer",
    "text-transform": "uppercase",
    "font-weight": "550",
    "background": "#2d3b55",
    "color": "#fff",
    "border-bottom-left-radius": "4px",
    "border-bottom-right-radius": "0",
    "letter-spacing": "0.2px",
    "outline": "0",
    "-webkit-transition": "all .3s",
    "transition": "all .3s",
}

const SearchButtonHover = {
    
    "background": "#3c4d6d",

}

const Input = {
    "border": 0,
    "margin-right": "20px",
    "padding": "5px 1px",
    "borderBottom": "1px solid #747474",
}

